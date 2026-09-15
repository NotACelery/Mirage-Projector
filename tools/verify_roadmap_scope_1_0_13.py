#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def need(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

w11=read('docs/WAITLIST-1.1.0.md')
w12=read('docs/WAITLIST-1.2.0.md')
road=read('docs/ROADMAP.md')
audit=read('docs/history/audits/PRE-1.0.14-ROADMAP-AUDIT.md')

need(any(h in w11 for h in ('## 1.1.0 delivery map after the 1.0.13 audit','## 1.1.0 delivery map after the 1.0.17 audit','## 1.1.0 delivery map after the 1.0.18 stabilization audit','## 1.1.0 delivery map after the 1.0.19 QA follow-up')), '1.1 delivery map missing')
need('Shoulder Strap Battery Pouch / upgrade system' in w11, '1.1 shoulder pouch contract missing')
need('6 Battery Pouch slots' in w11 and '6 to 9 battery slots' in w11, '1.1 pouch capacity contract missing')
need('Auto Battery Swap Patch' in w11 and ('Battery Pouch Expansion Patch' in w11 or 'Shoulder Strap Slot Expansion' in w11), '1.1 upgrade set missing')
need('Explicitly deferred to 1.2.0' in w11, '1.1 does not explicitly exclude 1.2 scope')
need('UV Shoulder Light' in w12, '1.2 UV shoulder light missing')
need('Auto UV Patch' in w12, '1.2 Auto UV missing')
need('UV Marks' in w12, '1.2 UV Marks missing')
need('direct sunlight' in w12, '1.2 sunlight suppression contract missing')
need('Focus UV' in w12 and 'Flood UV' in w12 and 'Ambient UV' in w12, '1.2 per-mode UV behavior missing')
need('Interactive holograms & UV ecosystem' in road, 'roadmap 1.2 theme not updated')
need('MIRAGE PROJECTOR 1.0.13 VERIFICATION PASS (30 gates)' in audit, 'pre-1.0.14 baseline audit result missing')

if errors:
    print('Mirage roadmap/scope audit FAILED')
    for e in errors: print(' -',e)
    sys.exit(1)
print('Mirage roadmap/scope audit PASS')
