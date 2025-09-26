from pathlib import Path
path = Path(r"D:\finance_analyse-master\frontend\src\App.vue")
text = path.read_text(encoding='utf-8')
text = text.replace('    const data = await analyzeSingleStock(code);', "    const response = await analyzeStock({ stockCode: code, analysisType: 'STRUCTURED' });", 1)
text = text.replace('    singleStockData.value = data;', '    singleStockData.value = toExtractedJson(response.analysis as SingleStockApiResponse);', 1)
success_line = '    ElMessage.success(`�ѻ�ȡ ${code} �Ľṹ������`);'
if success_line not in text:
    raise SystemExit('success line not found')
text = text.replace(success_line, success_line + '\n    await refreshStockHistory();', 1)
path.write_text(text, encoding='utf-8')
