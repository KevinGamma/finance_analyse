#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Helper script to fetch real-time K-line data via Futu Open API."""
from __future__ import annotations

import argparse
import json
import os
import sys
from typing import Any, Dict, List

try:
    from futu import (
        AuType,
        KLType,
        RET_ERROR,
        RET_OK,
        Session,
        SubType,
        OpenQuoteContext,
    )
except ImportError as exc:  # pragma: no cover - runtime dependency
    sys.stdout.write(
        json.dumps(
            {
                "ret": str(RET_ERROR) if "RET_ERROR" in globals() else "-1",
                "message": f"Missing futu dependency: {exc}",
                "data": [],
            }
        )
    )
    sys.exit(0)


DEFAULT_HOST = os.environ.get("FUTU_OPEND_HOST", "127.0.0.1")
DEFAULT_PORT = int(os.environ.get("FUTU_OPEND_PORT", "11111"))


def normalize_enum_value(enum_cls, value: str):
    if not value:
        raise ValueError("Enum value can not be empty")
    candidate = value.split(".")[-1].strip()
    try:
        return getattr(enum_cls, candidate)
    except AttributeError as exc:
        raise ValueError(f"Invalid value '{value}' for {enum_cls.__name__}") from exc


def convert_records(records) -> List[Dict[str, Any]]:
    normalized: List[Dict[str, Any]] = []
    for item in records:
        normalized.append(
            {
                "code": item.get("code"),
                "name": item.get("name"),
                "timeKey": item.get("time_key"),
                "open": item.get("open"),
                "close": item.get("close"),
                "high": item.get("high"),
                "low": item.get("low"),
                "volume": item.get("volume"),
                "turnover": item.get("turnover"),
                "peRatio": item.get("pe_ratio"),
                "turnoverRate": item.get("turnover_rate"),
                "lastClose": item.get("last_close"),
            }
        )
    return normalized


def main() -> int:
    parser = argparse.ArgumentParser(description="Fetch K-line data via Futu Open API")
    parser.add_argument("--code", required=True, help="Stock code, e.g. US.AAPL")
    parser.add_argument("--num", type=int, default=120, help="Number of K-line points")
    parser.add_argument("--ktype", default="K_DAY", help="KLType name, e.g. K_DAY")
    parser.add_argument("--autype", default="QFQ", help="AuType name, e.g. QFQ")
    parser.add_argument("--host", default=DEFAULT_HOST, help="OpenD host")
    parser.add_argument("--port", type=int, default=DEFAULT_PORT, help="OpenD port")
    parser.add_argument("--session", default="ALL", help="Session enum value")

    args = parser.parse_args()

    try:
        kl_type = normalize_enum_value(KLType, args.ktype)
        au_type = normalize_enum_value(AuType, args.autype)
        sub_type = normalize_enum_value(SubType, args.ktype)
        session = normalize_enum_value(Session, args.session)
    except ValueError as exc:
        sys.stdout.write(json.dumps({"ret": str(RET_ERROR), "message": str(exc), "data": []}))
        return 0

    ctx = OpenQuoteContext(host=args.host, port=args.port)
    try:
        ret_sub, err = ctx.subscribe([args.code], [sub_type], subscribe_push=False, session=session)
        if ret_sub != RET_OK:
            sys.stdout.write(json.dumps({"ret": str(ret_sub), "message": err, "data": []}))
            return 0

        ret, data = ctx.get_cur_kline(args.code, args.num, kl_type, au_type)
        if ret != RET_OK:
            sys.stdout.write(json.dumps({"ret": str(ret), "message": data, "data": []}))
            return 0

        records = data.where(data.notna(), None).to_dict("records")  # type: ignore[attr-defined]
        result = {
            "ret": str(ret),
            "message": "",
            "data": convert_records(records),
        }
        sys.stdout.write(json.dumps(result, ensure_ascii=False))
        return 0
    except Exception as exc:  # pragma: no cover - defensive
        sys.stdout.write(json.dumps({"ret": str(RET_ERROR), "message": str(exc), "data": []}))
        return 0
    finally:
        ctx.close()


if __name__ == "__main__":
    sys.exit(main())
