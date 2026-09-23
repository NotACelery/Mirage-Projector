#!/usr/bin/env python3
"""Run the cumulative Mirage Projector regression suite."""

from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
TOOLS = ROOT / 'tools'

SUITE = [
    'verify_dev76b_cleanup_contract.py',
    'verify_dev76c_atomic_static_publication.py',
    'verify_dev76d_build_cleanup.py',
    'verify_dev76e_chunk_snapshot_handshake.py',
    'verify_dev76f_atomic_chunk_snapshot.py',
    'verify_dev77_booster_math.py',
    'verify_dev77_core_booster_identity.py',
    'verify_mirage_light_curve.py',
    'verify_mirage_light_dependency_window.py',
    'verify_mirage_light_detour_penalty.py',
    'verify_mirage_light_occlusion_contract.py',
    'verify_mirage_light_solver_model.py',
    'verify_virtual_light_invalidation.py',
    'verify_dev80f_selective_compact_firstperson_lift.py',
    'verify_dev82_projection_source_foundation.py',
    'verify_dev82_mod_logo_integration.py',
    'verify_dev86a_compile_hotfix.py',
    'verify_release_responsive_scroll.py',
    'verify_1_0_7_prism_compaction_and_tabs.py',
    'verify_1_0_7_dynamic_light_foundation.py',
    'verify_1_0_7_glow_dust_battery.py',
    'verify_1_0_9_light_projector.py',
    'verify_1_0_10_lantern.py',
    'verify_1_0_11_hand_projector.py',
    'verify_1_0_12_portable_persistence.py',
    'verify_1_0_13_shoulder_equipment.py',
    'verify_roadmap_scope_1_0_13.py',
    'verify_1_0_14_shoulder_pouch.py',
    'verify_1_0_15_charging_station.py',
    'verify_1_0_16_war_banner.py',
    'verify_1_0_17_scan_codex.py',
    'verify_1_0_18_stabilization.py',
    'verify_1_0_18_integrity_cleanup.py',
    'verify_1_0_19_qa_followup.py',
    'verify_release_1_0_10.py',
    'verify_release_1_0_11.py',
    'verify_release_1_0_12.py',
    'verify_release_1_0_13.py',
    'verify_release_1_0_14.py',
    'verify_release_1_0_15.py',
    'verify_release_1_0_16.py',
    'verify_release_1_0_17.py',
    'verify_release_1_0_18.py',
    'verify_1_0_20_sodium_light_bridge.py',
    'verify_release_1_0_20.py',
    'verify_1_0_21_runtime_qa.py',
    'verify_1_0_23_vanilla_lectern_codex.py',
    'verify_1_0_24_codex_library_card_rework.py',
    'verify_1_0_25_anchor_chassis_foundation.py',
    'verify_release_1_0_25.py',
    'verify_1_0_26_presentation_control.py',
    'verify_release_1_0_26.py',
    'verify_1_0_27_build_stability.py',
    'verify_release_1_0_27.py',
    'verify_1_0_28_recipe_sync.py',
    'verify_release_1_0_28.py',
    'verify_1_0_29_runtime_qa_interactions.py',
    'verify_release_1_0_29.py',
    'verify_1_0_30_ux_runtime_wave.py',
    'verify_release_1_0_30.py',
    'verify_1_0_31_table_runtime_rebuild.py',
    'verify_release_1_0_31.py',
    'verify_1_0_32_survival_progression.py',
    'verify_1_0_32_recipe_viewers.py',
    'verify_1_0_32_overlay_cleanup.py',
    'verify_1_0_32_table_rotation_visibility.py',
    'verify_1_0_32_table_dedicated_runtime.py',
    'verify_release_1_0_32.py',
    'verify_1_0_33_end_resonance_foundation.py',
    'verify_release_1_0_33.py',
    'verify_1_0_34_roadmap_ux_cleanup.py',
    'verify_release_1_0_34.py',
]

for name in SUITE:
    print(f'\n== {name} ==', flush=True)
    code = subprocess.run([sys.executable, str(TOOLS / name)], cwd=ROOT).returncode
    if code:
        raise SystemExit(code)

print(f'\nMIRAGE PROJECTOR CURRENT-LINE VERIFICATION PASS ({len(SUITE)} gates)')
