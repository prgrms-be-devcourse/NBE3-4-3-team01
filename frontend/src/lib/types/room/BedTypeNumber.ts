export const BED_TYPES = [
  "SINGLE",
  "DOUBLE",
  "QUEEN",
  "KING",
  "TWIN",
  "TRIPLE",
] as const;

export type BedType = (typeof BED_TYPES)[number];

export interface BedTypeNumber {
  SINGLE: number;
  DOUBLE: number;
  QUEEN: number;
  KING: number;
  TWIN: number;
  TRIPLE: number;
}
