export interface TransferSwap {
  playerOut: string;
  playerIn: string;
  pointsOut: number;
  pointsIn: number;
  /** Negative when the sold player outscored the bought one. */
  swapImpact: number;
}

export interface TransferImpactGw {
  gameWeek: number;
  /** Net swap impact for the GW. Negative = points lost. */
  swapImpact: number;
  /** Hit penalty (0, -4, -8 …). Always zero or negative. */
  hitCost: number;
  transfers: TransferSwap[];
}

export interface TransferImpact {
  totalSwapPointsLost: number;
  totalHitPointsLost: number;
  perGameWeek: TransferImpactGw[];
}
