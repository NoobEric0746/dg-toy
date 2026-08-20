package org.nooberic.dg_toy.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.ShockEffectHelper;

/**
 * 电击附加
 * <p>
 * 与火焰附加保持一致：仅可附魔在剑上，最大等级 4。
 * 命中玩家时，使目标受到 1 秒 const 波形电击（soft 强度模式）。
 */
public class ElectricShockEnchantment extends Enchantment {

    /** 双通道（A+B），与项目内其它惩罚类电击保持一致。 */
    private static final int SHOCK_CHANNEL = 3;
    /** const 波形。 */
    private static final String SHOCK_PULSE_ID = "const";
    /** soft 强度模式。 */
    private static final boolean SOFT_MODE = false;
    /** 电击时长（秒）。 */
    private static final int SHOCK_DURATION_SECONDS = 1;

    public ElectricShockEnchantment() {
        super(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + 20 * (level - 1);
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        // 附魔在攻击者(attacker)手持武器上，由 EnchantmentHelper.doPostDamageEffects 遍历攻击者装备触发；
        // 第二个参数 target 才是被打中的实体，只对被击中的玩家生效。
        if (!(target instanceof ServerPlayer player)) {
            return;
        }
        // 未装备郊狼主机（无法接收电击）时跳过
        if (!RemoteControlStateHelper.isShockAvailable(player)) {
            return;
        }
        ShockEffectHelper.apply(player, SOFT_MODE, SHOCK_CHANNEL, strengthForLevel(level), SHOCK_PULSE_ID, SHOCK_DURATION_SECONDS);
    }

    /**
     * 根据附魔等级计算电击强度。
     * <p>
     * 生存模式通过附魔台最高只能获得 4 级，但铁砧/命令等方式可能获得更高等级，
     * 因此这里对 5 级及以上的等级也做了兜底处理。
     */
    private static int strengthForLevel(int level) {
        if (level <= 1) {
            return 16;
        }
        if (level == 2) {
            return 33;
        }
        if (level == 3) {
            return 50;
        }
        if (level == 4) {
            return 66;
        }
        if (level == 5) {
            return 83;
        }
        return 100;
    }
}
