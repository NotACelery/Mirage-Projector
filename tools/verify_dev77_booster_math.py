def diffusion(glass, diamond):
    return max(0, min(4, glass - diamond))

def delta(glass=0, quartz=0, diamond=0):
    return max(-4, min(4, quartz + diamond // 2 - diffusion(glass, diamond)))

def detour(glass=0, diamond=0):
    relief = 1 if diffusion(glass, diamond) > 0 else 0
    focus = (diamond + 1) // 2
    return max(0, min(4, 1 + focus - relief))

def radius(**kw):
    return (15 + delta(**kw)) * 2

cases = {
    'base': (radius(), detour()),
    'glass1': (radius(glass=1), detour(glass=1)),
    'quartz1': (radius(quartz=1), detour()),
    'diamond1': (radius(diamond=1), detour(diamond=1)),
    'diamond2': (radius(diamond=2), detour(diamond=2)),
    'quartz4': (radius(quartz=4), detour()),
    'glass4': (radius(glass=4), detour(glass=4)),
    'glass1_diamond1': (radius(glass=1, diamond=1), detour(glass=1, diamond=1)),
}
expected = {
    'base': (30,1),
    'glass1': (28,0),
    'quartz1': (32,1),
    'diamond1': (30,2),
    'diamond2': (32,2),
    'quartz4': (38,1),
    'glass4': (22,0),
    'glass1_diamond1': (30,2),
}
for name, exp in expected.items():
    got=cases[name]
    assert got==exp, (name,got,exp)
    print('PASS',name,got)
print('dev.77 Booster math verification PASS')
