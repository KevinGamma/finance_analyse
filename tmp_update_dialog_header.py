from pathlib import Path
path = Path(r"D:\finance_analyse-master\frontend\src\App.vue")
text = path.read_text(encoding='utf-8')
old_line = "            {{ selectedStockHistory.stockCode }} �� {{ formatDate(selectedStockHistory.requestedAt) }}"
new_line = "            {{ selectedStockHistory.stockCode }} · {{ formatDate(selectedStockHistory.requestedAt) }} · {{ formatAnalysisType(selectedStockHistory.analysisType) }}"
if old_line not in text:
    raise SystemExit('stock dialog header line not found')
text = text.replace(old_line, new_line, 1)
path.write_text(text, encoding='utf-8')
