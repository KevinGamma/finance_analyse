from pathlib import Path
path = Path(r"D:\finance_analyse-master\frontend\src\App.vue")
text = path.read_text(encoding='utf-8')
old_block = "          <span v-if=\"selectedStockHistory\">\n            {{ selectedStockHistory.stockCode }} \uFFFD\uFFFD {{ formatDate(selectedStockHistory.requestedAt) }}\n          </span>"
new_block = "          <span v-if=\"selectedStockHistory\">\n            {{ selectedStockHistory.stockCode }} · {{ formatDate(selectedStockHistory.requestedAt) }} · {{ formatAnalysisType(selectedStockHistory.analysisType) }}\n          </span>"
if old_block not in text:
    raise SystemExit('stock dialog span block not found')
text = text.replace(old_block, new_block, 1)
path.write_text(text, encoding='utf-8')
