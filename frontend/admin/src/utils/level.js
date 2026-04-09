const LEVEL_THEMES = [
  {
    tier: 1,
    name: '晨光学徒',
    shortName: '晨光',
    gradient: 'linear-gradient(135deg, #7dd3fc 0%, #38bdf8 50%, #0ea5e9 100%)',
    textGradient: 'linear-gradient(135deg, #8be5ff 0%, #38bdf8 55%, #0ea5e9 100%)',
    shadow: 'rgba(56, 189, 248, 0.38)',
    border: 'rgba(125, 211, 252, 0.42)',
    effect: 'mist'
  },
  {
    tier: 2,
    name: '苍穹行者',
    shortName: '苍穹',
    gradient: 'linear-gradient(135deg, #60a5fa 0%, #6366f1 52%, #7c3aed 100%)',
    textGradient: 'linear-gradient(135deg, #93c5fd 0%, #818cf8 55%, #a78bfa 100%)',
    shadow: 'rgba(99, 102, 241, 0.42)',
    border: 'rgba(129, 140, 248, 0.42)',
    effect: 'flow'
  },
  {
    tier: 3,
    name: '极光使徒',
    shortName: '极光',
    gradient: 'linear-gradient(135deg, #34d399 0%, #14b8a6 45%, #0f766e 100%)',
    textGradient: 'linear-gradient(135deg, #6ee7b7 0%, #2dd4bf 52%, #14b8a6 100%)',
    shadow: 'rgba(20, 184, 166, 0.42)',
    border: 'rgba(110, 231, 183, 0.42)',
    effect: 'breathe'
  },
  {
    tier: 4,
    name: '熔金骑士',
    shortName: '熔金',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #f97316 50%, #ea580c 100%)',
    textGradient: 'linear-gradient(135deg, #fde68a 0%, #fbbf24 50%, #f97316 100%)',
    shadow: 'rgba(249, 115, 22, 0.42)',
    border: 'rgba(251, 191, 36, 0.42)',
    effect: 'shine'
  },
  {
    tier: 5,
    name: '曜石主宰',
    shortName: '曜石',
    gradient: 'linear-gradient(135deg, #a855f7 0%, #7c3aed 45%, #4c1d95 100%)',
    textGradient: 'linear-gradient(135deg, #d8b4fe 0%, #c084fc 50%, #8b5cf6 100%)',
    shadow: 'rgba(139, 92, 246, 0.45)',
    border: 'rgba(192, 132, 252, 0.4)',
    effect: 'orbit'
  },
  {
    tier: 6,
    name: '星海领主',
    shortName: '星海',
    gradient: 'linear-gradient(135deg, #2563eb 0%, #0f172a 48%, #38bdf8 100%)',
    textGradient: 'linear-gradient(135deg, #93c5fd 0%, #60a5fa 48%, #67e8f9 100%)',
    shadow: 'rgba(37, 99, 235, 0.45)',
    border: 'rgba(103, 232, 249, 0.42)',
    effect: 'galaxy'
  },
  {
    tier: 7,
    name: '绯焰圣裁',
    shortName: '绯焰',
    gradient: 'linear-gradient(135deg, #fb7185 0%, #f43f5e 48%, #be123c 100%)',
    textGradient: 'linear-gradient(135deg, #fecdd3 0%, #fb7185 48%, #fda4af 100%)',
    shadow: 'rgba(244, 63, 94, 0.45)',
    border: 'rgba(251, 113, 133, 0.42)',
    effect: 'pulse'
  },
  {
    tier: 8,
    name: '神谕裁决',
    shortName: '神谕',
    gradient: 'linear-gradient(135deg, #f97316 0%, #ef4444 45%, #7f1d1d 100%)',
    textGradient: 'linear-gradient(135deg, #fdba74 0%, #fb7185 50%, #fca5a5 100%)',
    shadow: 'rgba(239, 68, 68, 0.48)',
    border: 'rgba(253, 186, 116, 0.42)',
    effect: 'meteor'
  },
  {
    tier: 9,
    name: '寰宇神子',
    shortName: '寰宇',
    gradient: 'linear-gradient(135deg, #22d3ee 0%, #3b82f6 40%, #8b5cf6 78%, #ec4899 100%)',
    textGradient: 'linear-gradient(135deg, #67e8f9 0%, #93c5fd 35%, #c4b5fd 70%, #f9a8d4 100%)',
    shadow: 'rgba(59, 130, 246, 0.52)',
    border: 'rgba(196, 181, 253, 0.45)',
    effect: 'spectrum'
  },
  {
    tier: 10,
    name: '天穹王座',
    shortName: '天穹',
    gradient: 'linear-gradient(135deg, #fde68a 0%, #facc15 30%, #e879f9 72%, #fef3c7 100%)',
    textGradient: 'linear-gradient(135deg, #fef9c3 0%, #fde68a 35%, #f5d0fe 70%, #ffffff 100%)',
    shadow: 'rgba(250, 204, 21, 0.55)',
    border: 'rgba(253, 224, 71, 0.5)',
    effect: 'crown'
  },
  {
    tier: 11,
    name: '创世神明',
    shortName: '创世',
    gradient: 'linear-gradient(135deg, #ff00cc 0%, #333399 50%, #00ffff 100%)',
    textGradient: 'linear-gradient(135deg, #ff66ff 0%, #6666ff 50%, #66ffff 100%)',
    shadow: 'rgba(255, 0, 204, 0.6)',
    border: 'rgba(51, 51, 153, 0.6)',
    effect: 'genesis'
  }
]

export function normalizeUserLevel(level) {
  const value = Number(level)
  if (!Number.isFinite(value)) {
    return 1
  }
  return Math.min(100, Math.max(1, Math.round(value)))
}

export function getUserLevelTier(level) {
  const normalizedLevel = normalizeUserLevel(level)
  if (normalizedLevel === 100) return 11 // 100级专属Tier
  return Math.ceil(normalizedLevel / 10)
}

export function getUserLevelTheme(level) {
  const normalizedLevel = normalizeUserLevel(level)
  const tier = getUserLevelTier(normalizedLevel)
  const theme = LEVEL_THEMES[tier - 1]
  const minLevel = tier === 11 ? 100 : (tier - 1) * 10 + 1
  const maxLevel = tier === 11 ? 100 : tier * 10

  return {
    ...theme,
    level: normalizedLevel,
    tier,
    minLevel,
    maxLevel,
    label: `Lv.${String(normalizedLevel).padStart(2, '0')}`
  }
}

export function getAllUserLevelThemes() {
  return LEVEL_THEMES.map((theme, index) => {
    if (index === 10) {
      return {
        ...theme,
        minLevel: 100,
        maxLevel: 100
      }
    }
    return {
      ...theme,
      minLevel: index * 10 + 1,
      maxLevel: (index + 1) * 10
    }
  })
}
