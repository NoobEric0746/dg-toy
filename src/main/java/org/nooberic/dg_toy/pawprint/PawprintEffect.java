package org.nooberic.dg_toy.pawprint;

public record PawprintEffect(int channel, int strength, boolean hardMode, String pulseId, int duration) {
}