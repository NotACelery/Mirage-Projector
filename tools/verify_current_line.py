#!/usr/bin/env python3
"""Run the current Mirage Projector verification suite for dev.80."""
from pathlib import Path
import subprocess,sys
ROOT=Path(__file__).resolve().parents[1]; TOOLS=ROOT/'tools'
SUITE=[
'verify_dev76b_cleanup_contract.py','verify_dev76c_atomic_static_publication.py','verify_dev76d_build_cleanup.py',
'verify_dev76e_chunk_snapshot_handshake.py','verify_dev76f_atomic_chunk_snapshot.py',
'verify_dev77_booster_math.py','verify_dev77_core_booster_identity.py',
'verify_mirage_light_curve.py','verify_mirage_light_dependency_window.py','verify_mirage_light_detour_penalty.py',
'verify_mirage_light_occlusion_contract.py','verify_mirage_light_solver_model.py','verify_virtual_light_invalidation.py',
'verify_dev80_projector_promotion.py','verify_dev80_full_audit.py']
for name in SUITE:
    print(f'\n== {name} ==',flush=True)
    code=subprocess.run([sys.executable,str(TOOLS/name)],cwd=ROOT).returncode
    if code: raise SystemExit(code)
print(f'\nCURRENT-LINE VERIFICATION PASS ({len(SUITE)} gates)')
