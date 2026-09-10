/** Base URL for Premier League player photo resources. */
export const FPL_PLAYER_IMAGE_PRIMARY_URL = (code: number): string =>
  `https://resources.premierleague.com/premierleague25/photos/players/110x140/${code}.png`;

/** Fallback URL for player photos using the legacy path format. */
export const FPL_PLAYER_IMAGE_FALLBACK_URL = (code: number): string =>
  `https://resources.premierleague.com/premierleague/photos/players/110x140/p${code}.png`;
