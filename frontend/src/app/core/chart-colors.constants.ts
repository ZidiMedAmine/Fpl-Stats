/**
 * Centralised colour palette for Chart.js configurations.
 *
 * CSS custom properties (--color-*) are not accessible at class-field
 * initialisation time, so chart configs reference this constant instead.
 * Keep values in sync with the :root block in styles.css.
 */
export const CHART_COLORS = {
  /** FPL brand purple — matches CSS --color-primary-dark (#37003c). */
  primary:        '#37003c',
  primaryAlpha07: 'rgba(55, 0, 60, 0.07)',
  primaryAlpha10: 'rgba(55, 0, 60, 0.10)',
  primaryAlpha15: 'rgba(55, 0, 60, 0.15)',
  primaryAlpha25: 'rgba(55, 0, 60, 0.25)',
  primaryAlpha70: 'rgba(55, 0, 60, 0.70)',

  /** Positive delta / rank improvement — matches CSS --color-success (#4caf50). */
  success:        '#4caf50',
  successAlpha15: 'rgba(76, 175, 80, 0.15)',

  /** Negative delta / rank drop — matches CSS --color-rank-down (#f44336). */
  danger:         '#f44336',
  dangerAlpha15:  'rgba(244, 67, 54, 0.15)',

  /** Captain / benchmark / total-wealth highlight (gold). */
  gold:        '#e6a817',
  goldAlpha07: 'rgba(230, 168, 23, 0.07)',
  goldAlpha20: 'rgba(230, 168, 23, 0.20)',

  /** Second team in compare-teams charts (cyan). */
  team2:        '#00c3ff',
  team2Alpha10: 'rgba(0, 195, 255, 0.10)',
  team2Alpha70: 'rgba(0, 195, 255, 0.70)',

  /** Player B in compare-players charts (deep indigo). */
  playerB:        '#1a237e',
  playerBAlpha10: 'rgba(26, 35, 126, 0.10)',

  /** Second radar metric in compare-players charts (teal). */
  teal:        '#00b3a4',
  tealAlpha20: 'rgba(0, 179, 164, 0.20)',

  /** GW average / bench points line (pink). */
  pink:        '#e66b99',
  pinkAlpha25: 'rgba(230, 107, 153, 0.25)',

  /** Position breakdown colours — match CSS --color-gkp/def/mid/fwd variables. */
  position: {
    gkp: '#ffc107',
    def: '#4caf50',
    mid: '#2196f3',
    fwd: '#f44336',
  },

  /** Chart axis / grid UI colours (not exposed as CSS variables). */
  ui: {
    axisLabel:  '#111827',
    tickLabel:  '#4b5563',
    gridLine:   'rgba(0, 0, 0, 0.06)',
    gridLineMd: 'rgba(0, 0, 0, 0.08)',
    white:      '#ffffff',
    fallback:   '#999',
  },
} as const;
