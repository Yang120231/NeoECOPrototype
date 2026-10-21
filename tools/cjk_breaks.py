"""Report - and optionally fix - Chinese runs that guideme cannot line-wrap.

Why: guideme's flow layout only breaks a line at spaces (LineBuilder.iterateRuns is driven by the
space char) and MC's font has no CJK word breaker, so a run of Chinese with no spaces is one
unbreakable word. Table columns are equal width no matter what the content is
(LytTable.computeLayout: (available - columns - 1) / columns), so a long run in a narrow column is
drawn over the next column instead of folding.

Fix shape: a break point may only be placed where a human would break - after Chinese punctuation.
Inserting one every N characters cuts words in half ("公 开", "取 出"), which reads worse than the
overflow it cures, so a run with no punctuation inside its budget is reported instead of mangled.

Idempotent: existing spaces are already break points, so re-running adds nothing.

Usage:
    python tools/cjk_breaks.py <file.md> [...]          # report only
    python tools/cjk_breaks.py --fix <file.md> [...]    # insert the safe breaks
"""

import sys

PAGE_WIDTH_CJK = 52  # measured, not estimated - see PROSE_BUDGET below
# Measured in game on 2026-10-21 with the calibration ruler appended to the storage page: a full
# line holds ~52 CJK glyphs, a 3-column cell fits 16 and overflows at 18, a 5-column cell fits 11
# and overflows at 12. Columns are equal width whatever the content is (LytTable.computeLayout),
# so "page / columns" is the right shape and the -1 keeps the margin the ruler showed.
PROSE_BUDGET = 51
TABLE_LIMIT_FLOOR = 8  # never fold tighter than this, or a 6-column table turns into confetti
BREAK_AFTER = "，、；：。）】"  # places a human would break; the mark stays on the line before it


def is_cjk(ch):
    return "一" <= ch <= "鿿" or ch in BREAK_AFTER or ch in "（【《“"


def longest_run(text):
    """The longest stretch of CJK with no break point in it, measured from the previous break."""
    best = current = 0
    for ch in text:
        if ch == " " or (BREAK_AFTER and ch in BREAK_AFTER and current):
            # a space or "after punctuation" ends the run; punctuation itself still counts as width
            current = 1 if ch in BREAK_AFTER else 0
            best = max(best, current)
            continue
        current = current + 1 if is_cjk(ch) else 0
        best = max(best, current)
    return best


def insert_breaks(text, budget):
    """Put a space after punctuation whenever the run since the last break point exceeds budget."""
    out = []
    run = 0
    index = 0
    while index < len(text):
        ch = text[index]
        index += 1
        if ch == " ":
            run = 0
            out.append(ch)
            continue
        run = run + 1 if is_cjk(ch) else 0
        out.append(ch)
        if ch in BREAK_AFTER and run >= budget and index < len(text) and text[index] != " ":
            out.append(" ")
            run = 0
    return "".join(out)


def budget_for(line):
    if line.startswith("|"):
        return max(PAGE_WIDTH_CJK // max(line.count("|") - 1, 1) - 1, TABLE_LIMIT_FLOOR)
    return PROSE_BUDGET


def process(path, fix):
    with open(path, encoding="utf-8", newline="") as handle:
        source = handle.read()
    newline = "\r\n" if "\r\n" in source else "\n"
    problems = []
    out = []
    changed = 0
    for number, line in enumerate(source.split(newline), start=1):
        if line.lstrip().startswith("#") or line.lstrip().startswith("<") or not line.strip():
            out.append(line)
            continue
        budget = budget_for(line)
        if line.startswith("|") and line.count("|") > 1:
            fixed = "|".join(insert_breaks(cell, budget) for cell in line.split("|"))
        else:
            fixed = insert_breaks(line, budget)
        over = [cell for cell in fixed.split("|") if longest_run(cell) > budget] if fixed.startswith("|") \
            else ([] if longest_run(fixed) <= budget else [fixed])
        problems.extend((number, budget, cell.strip()) for cell in over if cell.strip())
        if fixed != line:
            changed += 1
        out.append(fixed if fix else line)
    if fix:
        with open(path, "w", encoding="utf-8", newline="") as handle:
            handle.write(newline.join(out))
    print("%s: %d line(s) %s" % (path, changed, "rewritten" if fix else "would change"))
    for number, budget, text in problems:
        print("   line %d: %d 字预算内没有可断的标点 -> %s" % (number, budget, text[:70]))
    return len(problems)


if __name__ == "__main__":
    args = sys.argv[1:]
    apply = "--fix" in args
    targets = [a for a in args if a != "--fix"]
    total = 0
    for target in targets:
        total += process(target, apply)
    print("需要拆句的句子：%d" % total)
