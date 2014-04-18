package net.minecraft.server;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class EntityCreeper extends EntityMonster {

    private int bp;
    private int fuseTicks;
    private int maxFuseTicks = 30;
    private int explosionRadius = 3;
    public Item record = null; // CraftBukkit

    public EntityCreeper(World world) {
        super(world);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalSwell(this));
        this.goalSelector.a(3, new PathfinderGoalAvoidPlayer(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 0.8D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
        this.targetSelector.a(2, new PathfinderGoalHurtByTarget(this, false));
    }

    protected void aC() {
        super.aC();
        this.getAttributeInstance(GenericAttributes.d).setValue(0.25D);
    }

    public boolean bj() {
        return true;
    }

    public int aw() {
        return this.getGoalTarget() == null ? 3 : 3 + (int) (this.getHealth() - 1.0F);
    }

    protected void b(float f) {
        super.b(f);
        this.fuseTicks = (int) ((float) this.fuseTicks + f * 1.5F);
        if (this.fuseTicks > this.maxFuseTicks - 5) {
            this.fuseTicks = this.maxFuseTicks - 5;
        }
    }

    protected void c() {
        super.c();
        this.datawatcher.a(16, Byte.valueOf((byte) -1));
        this.datawatcher.a(17, Byte.valueOf((byte) 0));
        this.datawatcher.a(18, Byte.valueOf((byte) 0));
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.datawatcher.getByte(17) == 1) {
            nbttagcompound.setBoolean("powered", true);
        }

        nbttagcompound.setShort("Fuse", (short) this.maxFuseTicks);
        nbttagcompound.setByte("ExplosionRadius", (byte) this.explosionRadius);
        nbttagcompound.setBoolean("ignited", this.cc());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.datawatcher.watch(17, Byte.valueOf((byte) (nbttagcompound.getBoolean("powered") ? 1 : 0)));
        if (nbttagcompound.hasKeyOfType("Fuse", 99)) {
            this.maxFuseTicks = nbttagcompound.getShort("Fuse");
        }

        if (nbttagcompound.hasKeyOfType("ExplosionRadius", 99)) {
            this.explosionRadius = nbttagcompound.getByte("ExplosionRadius");
        }

        if (nbttagcompound.getBoolean("ignited")) {
            this.cd();
        }
    }

    public void h() {
        if (this.isAlive()) {
            this.bp = this.fuseTicks;
            if (this.cc()) {
                this.a(1);
            }

            int i = this.cb();

            if (i > 0 && this.fuseTicks == 0) {
                this.makeSound("creeper.primed", 1.0F, 0.5F);
            }

            this.fuseTicks += i;
            if (this.fuseTicks < 0) {
                this.fuseTicks = 0;
            }

            if (this.fuseTicks >= this.maxFuseTicks) {
                this.fuseTicks = this.maxFuseTicks;
                this.ce();
            }
        }

        super.h();
    }

    protected String aS() {
        return "mob.creeper.say";
    }

    protected String aT() {
        return "mob.creeper.death";
    }

    public void die(DamageSource damagesource) {
        // CraftBukkit start - Rearranged the method (super call to end, drop to dropDeathLoot)
        if (damagesource.getEntity() instanceof EntitySkeleton) {
            int i = Item.b(Items.RECORD_1);
            int j = Item.b(Items.RECORD_12);
            int k = i + this.random.nextInt(j - i + 1);

            // this.a(Item.d(k), 1);
            this.record = Item.d(k);
        }

        super.die(damagesource);
        // CraftBukkit end
    }

    public boolean n(Entity entity) {
        return true;
    }

    public boolean isPowered() {
        return this.datawatcher.getByte(17) == 1;
    }

    protected Item getLoot() {
        return Items.SULPHUR;
    }

    public int cb() {
        return this.datawatcher.getByte(16);
    }

    public void a(int i) {
        this.datawatcher.watch(16, Byte.valueOf((byte) i));
    }

    public void a(EntityLightning entitylightning) {
        super.a(entitylightning);
        // CraftBukkit start
        if (CraftEventFactory.callCreeperPowerEvent(this, entitylightning, org.bukkit.event.entity.CreeperPowerEvent.PowerCause.LIGHTNING).isCancelled()) {
            return;
        }
        // CraftBukkit end
        this.datawatcher.watch(17, Byte.valueOf((byte) 1));
    }

    protected boolean a(EntityHuman entityhuman) {
        ItemStack itemstack = entityhuman.inventory.getItemInHand();

        if (itemstack != null && itemstack.getItem() == Items.FLINT_AND_STEEL) {
            this.world.makeSound(this.locX + 0.5D, this.locY + 0.5D, this.locZ + 0.5D, "fire.ignite", 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
            entityhuman.aZ();
            if (!this.world.isStatic) {
                this.cd();
                itemstack.damage(1, entityhuman);
                return true;
            }
        }

        return super.a(entityhuman);
    }

    private void ce() {
        if (!this.world.isStatic) {
            boolean flag = this.world.getGameRules().getBoolean("mobGriefing");

            /* CraftBukkit start - handled in event processing
            if (this.isPowered()) {
                this.world.explode(this, this.locX, this.locY, this.locZ, (float) (this.explosionRadius * 2), flag);
            } else {
                this.world.explode(this, this.locX, this.locY, this.locZ, (float) this.explosionRadius, flag);
            } */
            if (!CraftEventFactory.handleExplosionPrimeEvent(this, this.locX, this.locY, this.locZ, this.isPowered() ? this.explosionRadius * 2 : this.explosionRadius, false, flag)) {
                this.fuseTicks = 0;
                return;
            }
            // CraftBukkit end

            this.die();
        }
    }

    public boolean cc() {
        return this.datawatcher.getByte(18) != 0;
    }

    public void cd() {
        this.datawatcher.watch(18, Byte.valueOf((byte) 1));
    }
}
