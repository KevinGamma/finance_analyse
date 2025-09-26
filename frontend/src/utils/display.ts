export function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

export function toReadableText(value: unknown): string {
  const result = renderValue(value);
  return result || '暂无可显示的分析结果。';
}

function renderValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (trimmed && isJsonLike(trimmed)) {
      try {
        return renderValue(JSON.parse(trimmed));
      } catch {
        return formatMultilineContent(value);
      }
    }
    return formatMultilineContent(value);
  }
  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }
  if (Array.isArray(value)) {
    return value
      .map((item) => renderValue(item))
      .filter((item) => item.trim().length > 0)
      .join('\n\n');
  }
  if (typeof value === 'object') {
    const record = value as Record<string, unknown>;
    const maybeText = record.text;
    if (typeof maybeText === 'string') {
      return formatMultilineContent(maybeText);
    }
    return Object.entries(record)
      .map(([key, val]) => formatEntry(key, val))
      .filter((item) => item.trim().length > 0)
      .join('\n\n');
  }
  return '';
}

function formatEntry(key: string, value: unknown): string {
  const rendered = renderValue(value);
  if (!rendered) {
    return '';
  }
  const label = prettifyKey(key);
  if (rendered.includes('\n')) {
    return `${label}:\n${indentMultiline(rendered)}`;
  }
  return `${label}: ${rendered}`;
}

function prettifyKey(key: string): string {
  return key
    .replace(/[_-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\b\w/g, (match) => match.toUpperCase());
}

function indentMultiline(value: string): string {
  return value
    .split('\n')
    .map((line) => (line.trim() ? `  ${line}` : line))
    .join('\n');
}

function isJsonLike(text: string): boolean {
  const trimmed = text.trim();
  return (
    (trimmed.startsWith('{') && trimmed.endsWith('}')) ||
    (trimmed.startsWith('[') && trimmed.endsWith(']'))
  );
}

function formatMultilineContent(content: string): string {
  return content.replace(/\\n/g, '\n').trim();
}
