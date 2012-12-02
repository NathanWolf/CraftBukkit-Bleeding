package org.bukkit.craftbukkit.inventory;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Joiner;

@RunWith(Parameterized.class)
public class BookItemStackTest extends ItemStackTests {

    @Parameters(name="[{index}]:{" + NAME_PARAMETER + "}")
    public static List<Object[]> data() {
        return StackProvider.compound(operaters(), "%s %s", NAME_PARAMETER, Material.WRITTEN_BOOK, Material.BOOK_AND_QUILL);
    }

    static List<Object[]> operaters() {
        return CompoundOperater.compound(
            Joiner.on('+'),
            NAME_PARAMETER,
            Long.parseLong("110", 2),
            ItemStackLoreEnchantmentTest.operaters(),
            Arrays.asList(
                new Object[] {
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.addPage("Page 1", "Page 2");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            return cleanStack;
                        }
                    },
                    "Pages vs. Null"
                },
                new Object[] {
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.addPage("Page 1", "Page 2");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            cleanStack.setItemMeta(cleanStack.getItemMeta());
                            return cleanStack;
                        }
                    },
                    "Pages vs. blank"
                },
                new Object[] {
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.addPage("Page 1", "Page 2");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.addPage("Page 2", "Page 1");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    "Pages switched"
                },
                new Object[] {
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.addPage("Page 1", "Page 2");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.addPage("Page 1");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    "Pages short"
                }
            ),
            Arrays.asList(
                new Object[] {
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.setAuthor("AnAuthor");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            return cleanStack;
                        }
                    },
                    "Author vs. Null"
                },
                new Object[] {
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.setAuthor("AnAuthor");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            cleanStack.setItemMeta(cleanStack.getItemMeta());
                            return cleanStack;
                        }
                    },
                    "Author vs. blank"
                },
                new Object[] {
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.setAuthor("AnAuthor");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    new Operater() {
                        public ItemStack operate(ItemStack cleanStack) {
                            BookMeta meta = (BookMeta) cleanStack.getItemMeta();
                            meta.setAuthor("AnotherAuthor");
                            cleanStack.setItemMeta(meta);
                            return cleanStack;
                        }
                    },
                    "Authors"
                }
            )
        );
    }
}
