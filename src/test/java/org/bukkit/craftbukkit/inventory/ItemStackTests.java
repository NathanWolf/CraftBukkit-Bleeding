package org.bukkit.craftbukkit.inventory;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.DummyServer;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

@RunWith(Parameterized.class)
public class ItemStackTests {
    static abstract class StackProvider {
        final Material material;

        StackProvider(Material material) {
            this.material = material;
        }

        ItemStack bukkit() {
            return operate(cleanStack(material, false));
        }

        ItemStack craft() {
            return operate(cleanStack(material, true));
        }

        abstract ItemStack operate(ItemStack cleanStack);

        static ItemStack cleanStack(Material material, boolean craft) {
            final ItemStack stack = new ItemStack(material);
            return craft ? CraftItemStack.asCraftCopy(stack) : stack;
        }

        /**
         * For each item in parameterList, it will apply nameFormat at nameIndex.
         * For each item in parameterList for each item in materials, it will create a stack provider at each array index that contains an Operater.
         *
         * @param parameterList
         * @param nameFormat
         * @param nameIndex
         * @param materials
         * @return
         */
        static List<Object[]> compound(final List<Object[]> parameterList, final String nameFormat, final int nameIndex, final Material...materials) {
            final List<Object[]> out = new ArrayList<Object[]>();
            for (Object[] params : parameterList) {
                final int len = params.length;
                for (final Material material : materials) {
                    final Object[] paramsOut = params.clone();
                    for (int i = 0; i < len; i++) {
                        final Object param = paramsOut[i];
                        if (param instanceof Operater) {
                            final Operater operater = (Operater) param;
                            paramsOut[i] = new StackProvider(material) {
                                @Override
                                ItemStack operate(ItemStack cleanStack) {
                                    return operater.operate(cleanStack);
                                }
                            };
                        }
                    }
                    paramsOut[nameIndex] = String.format(nameFormat, paramsOut[nameIndex], material);
                    out.add(paramsOut);
                }
            }
            return out;
        }
    }

    interface Operater {
        ItemStack operate(ItemStack cleanStack);
    }

    static class CompoundOperater implements Operater {
        static class RecursiveContainer {
            final Joiner joiner;
            final Object[] strings;
            final int nameParameter;
            final List<Object[]> stack;
            final List<Object[]> out;
            final List<Object[]>[] lists;

            RecursiveContainer(Joiner joiner, Object[] strings, int nameParameter, List<Object[]> stack, List<Object[]> out, List<Object[]>[] lists) {
                this.joiner = joiner;
                this.strings = strings;
                this.nameParameter = nameParameter;
                this.stack = stack;
                this.out = out;
                this.lists = lists;
            }
        }
        final Operater[] operaters;

        CompoundOperater(Operater...operaters) {
            this.operaters = operaters;
        }

        public ItemStack operate(ItemStack cleanStack) {
            for (Operater operater : operaters) {
                operater.operate(cleanStack);
            }
            return cleanStack;
        }


        /**
         * This combines different tests into one large collection, combining no two tests from the same list.
         * @param joiner used to join names
         * @param nameParameter index of the name parameter
         * @param singletonBitmask a list of bits representing the 'singletons' located in your originalLists. Lowest order bits represent the first items in originalLists.
         *      Singletons are exponentially linked with each other, such that,
         *      the output will contain every unique subset of only items from the singletons,
         *      as well as every unique subset that contains at least one item from each non-singleton.
         * @param originalLists
         * @return
         */
        static List<Object[]> compound(final Joiner joiner, final int nameParameter, final long singletonBitmask, final List<Object[]>...originalLists) {

            final List<Object[]> out = new ArrayList<Object[]>();
            final List<List<Object[]>> singletons = new ArrayList<List<Object[]>>();
            final List<List<Object[]>> notSingletons = new ArrayList<List<Object[]>>();

            { // Separate and prime the 'singletons'
                int i = 0;
                for (List<Object[]> list : originalLists) {
                    (((singletonBitmask >>> i++) & 0x1) == 0x1 ? singletons : notSingletons).add(list);
                }
            }

            for (final List<Object[]> primarySingleton : singletons) {
                // Iterate over our singletons, to multiply the 'out' each time
                for (final Object[] entry : out.toArray(EMPTY_ARRAY)) {
                    // Iterate over a snapshot of 'out' to prevent CMEs / infinite iteration
                    final int len = entry.length;
                    for (final Object[] singleton : primarySingleton) {
                        // Iterate over each item in our singleton for the current 'out' entry
                        final Object[] toOut = entry.clone();
                        for (int i = 0; i < len; i++) {
                            // Iterate over each parameter
                            if (i == nameParameter) {
                                toOut[i] = joiner.join(toOut[i], singleton[i]);
                            } else if (toOut[i] instanceof Operater) {
                                final Operater op1 = (Operater) toOut[i];
                                final Operater op2 = (Operater) singleton[i];
                                toOut[i] = new Operater() {
                                    public ItemStack operate(final ItemStack cleanStack) {
                                        return op2.operate(op1.operate(cleanStack));
                                    }
                                };
                            }
                        }
                        out.add(toOut);
                    }
                }
                out.addAll(primarySingleton);
            }

            final List<Object[]>[] lists = new List[notSingletons.size() + 1];
            notSingletons.toArray(lists);
            lists[lists.length - 1] = out;

            final RecursiveContainer methodParams = new RecursiveContainer(joiner, new Object[lists.length], nameParameter, new ArrayList<Object[]>(lists.length), new ArrayList<Object[]>(), lists);

            recursivelyCompound(methodParams, 0);
            methodParams.out.addAll(out);

            return methodParams.out;
        }

        private static void recursivelyCompound(final RecursiveContainer methodParams, final int level) {
            final List<Object[]> stack = methodParams.stack;

            if (level == methodParams.lists.length) {
                final Object[] firstParams = stack.get(0);
                final int len = firstParams.length;
                final int stackSize = stack.size();
                final Object[] params = new Object[len];

                for (int i = 0; i < len; i++) {
                    final Object firstParam = firstParams[i];

                    if (firstParam instanceof Operater) {
                        final Operater[] operaters = new Operater[stackSize];
                        for (int j = 0; j < stackSize; j++) {
                            operaters[j] = (Operater) stack.get(j)[i];
                        }

                        params[i] = new CompoundOperater(operaters);
                    } else if (i == methodParams.nameParameter) {
                        final Object[] strings = methodParams.strings;
                        for (int j = 0; j < stackSize; j++) {
                            strings[j] = stack.get(j)[i];
                        }

                        params[i] = methodParams.joiner.join(strings);
                    } else {
                        params[i] = firstParam;
                    }
                }

                methodParams.out.add(params);
            } else {
                final int marker = stack.size();

                for (final Object[] params : methodParams.lists[level]) {
                    stack.add(params);
                    recursivelyCompound(methodParams, level + 1);
                    stack.remove(marker);
                }
            }
        }
    }

    interface StackWrapper {
        ItemStack stack();
    }

    static class CraftWrapper implements StackWrapper {
        final StackProvider provider;

        CraftWrapper(StackProvider provider) {
            this.provider = provider;
        }

        public ItemStack stack() {
            return provider.craft();
        }
    }

    static class BukkitWrapper implements StackWrapper {
        final StackProvider provider;

        BukkitWrapper(StackProvider provider) {
            this.provider = provider;
        }

        public ItemStack stack() {
            return provider.bukkit();
        }
    }

    static class NoOpProvider extends StackProvider {

        NoOpProvider(Material material) {
            super(material);
        }

        @Override
        ItemStack operate(ItemStack cleanStack) {
            return cleanStack;
        }

    }

    @Parameters(name="[{index}]:{" + NAME_PARAMETER + "}")
    public static List<Object[]> data() {
        return ImmutableList.of(); // TODO, test basic durability issues
    }

    static final Object[][] EMPTY_ARRAY = new Object[0][];
    static final Material[] COMPOUND_MATERIALS;
    static final int NAME_PARAMETER = 2;
    static {
        DummyServer.setup();

        COMPOUND_MATERIALS = new Object() { // Workaround for JDK5
            Material[] value() {
                final ItemFactory factory = CraftItemFactory.instance();
                final Map<Class<? extends ItemMeta>, Material> possibleMaterials = new HashMap<Class<? extends ItemMeta>, Material>();
                for (final Material material : Material.values()) {
                    final ItemMeta meta = factory.getItemMeta(material);
                    if (meta == null || possibleMaterials.containsKey(meta.getClass()))
                        continue;
                    possibleMaterials.put(meta.getClass(), material);

                }
                return possibleMaterials.values().toArray(new Material[possibleMaterials.size()]);
            }
        }.value();
    }

    @Parameter(0) public StackProvider provider;
    @Parameter(1) public StackProvider unequalProvider;
    @Parameter(NAME_PARAMETER) public String name;

    @Test
    public void testBukkitInequality() {
        final StackWrapper bukkitWrapper = new CraftWrapper(provider);
        testInequality(bukkitWrapper, new BukkitWrapper(unequalProvider));
        testInequality(bukkitWrapper, new BukkitWrapper(new NoOpProvider(provider.material)));
    }

    @Test
    public void testCraftInequality() {
        final StackWrapper craftWrapper = new CraftWrapper(provider);
        testInequality(craftWrapper, new CraftWrapper(unequalProvider));
        testInequality(craftWrapper, new CraftWrapper(new NoOpProvider(provider.material)));
    }

    @Test
    public void testMixedInequality() {
        final StackWrapper craftWrapper = new CraftWrapper(provider);
        testInequality(craftWrapper, new BukkitWrapper(unequalProvider));
        testInequality(craftWrapper, new BukkitWrapper(new NoOpProvider(provider.material)));

        final StackWrapper bukkitWrapper = new CraftWrapper(provider);
        testInequality(bukkitWrapper, new CraftWrapper(unequalProvider));
        testInequality(bukkitWrapper, new CraftWrapper(new NoOpProvider(provider.material)));
    }

    static void testInequality(StackWrapper provider, StackWrapper unequalProvider) {
        final ItemStack stack = provider.stack();
        assertThat(stack, is(stack));

        final ItemStack unequalStack = unequalProvider.stack();
        assertThat(unequalStack, is(unequalStack));

        assertThat(stack, is(not(sameInstance(provider.stack()))));
        assertThat(stack, is(provider.stack()));
        assertThat(stack, is(not(unequalStack)));

        final ItemStack newStack = new ItemStack(stack);
        assertThat(newStack, is(stack));
        assertThat(newStack, is(not(unequalStack)));
        assertThat(newStack.getItemMeta(), is(stack.getItemMeta()));
        assertThat(newStack.getItemMeta(), is(not(unequalStack.getItemMeta())));

        final ItemStack craftStack = CraftItemStack.asCraftCopy(stack);
        assertThat(craftStack, is(stack));
        assertThat(craftStack, is(not(unequalStack)));
        assertThat(craftStack.getItemMeta(), is(stack.getItemMeta()));
        assertThat(craftStack.getItemMeta(), is(not(unequalStack.getItemMeta())));
    }

    @Test
    public void testBukkitDeserialize() {
        testDeserialize(new BukkitWrapper(provider), new BukkitWrapper(unequalProvider));
    }

    @Test
    public void testCraftDeserialize() {
        testDeserialize(new CraftWrapper(provider), new CraftWrapper(unequalProvider));
    }

    static void testDeserialize(StackWrapper provider, StackWrapper unequalProvider) {
        final ItemStack stack = provider.stack();
        final ItemStack unequalStack = unequalProvider.stack();
        final YamlConfiguration configOut = new YamlConfiguration();

        configOut.set("provider", stack);
        configOut.set("unequal", unequalStack);

        final String out = '\n' + configOut.saveToString();
        final YamlConfiguration configIn = new YamlConfiguration();

        try {
            configIn.loadFromString(out);
        } catch (InvalidConfigurationException ex) {
            throw new RuntimeException(out, ex);
        }

        assertThat(out, configIn.getItemStack("provider"), is(stack));
        assertThat(out, configIn.getItemStack("unequal"), is(unequalStack));
        assertThat(out, configIn.getItemStack("provider"), is(not(unequalStack)));
        assertThat(out, configIn.getItemStack("provider"), is(not(configIn.getItemStack("unequal"))));
    }
}
