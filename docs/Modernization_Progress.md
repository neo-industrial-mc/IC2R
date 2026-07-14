# IC2R Modernization Progress

**active_unit:** none  
**last_completed:** G3.1  
**updated:** 2026-07-14  

> 用户本会话：G1.7 + G1.8 收尾 → G3.1。

## Queue（G1 收尾）

| ID | status | last_notes |
|----|--------|------------|
| G1.1–G1.6 | done | 见历史 |
| G1.7 | done | Spotless/Checkstyle **skipped/N/A**（phase1 §10 决策，未启用） |
| G1.8 | done | Ic2rBlocks 门面 + 8 域类（247 字段）；compile+test 绿 |

## Queue（G2）

| ID | status | last_notes |
|----|--------|------------|
| G2.1–G2.8 | done | 阶段 2 gap 队列已完成 |

## Queue（G3）

| ID | status | last_notes |
|----|--------|------------|
| G3.1 | done | Dist/OnlyIn-only 34 文件清零 + debug 改 SPI；core Forge 文件 65→31、import 行 123→54；**仍 partial residual** |
| G3.2+ | pending | 见 phase3_closeout G3.* |

## Last session

- units: G1.7 → G1.8 → G3.1
- result: all PASS / done（G3.1 partial residual）
- suggested_commits: |
    1) `docs: skip Spotless/Checkstyle for G1.7`
    2) `refactor: split Ic2rBlocks into domain registration classes`
    3) `refactor: strip Dist/OnlyIn-only forge imports from core (G3.1 slice)`
- verify_log: |
    - G1.7: 文档 N/A skip ✅
    - G1.8: blocks/* 八域 + 门面；test 绿
    - G3.1: −34 files Dist-only；CommandIc2r → PlatformLifecycle；test 绿；未全清 residual
