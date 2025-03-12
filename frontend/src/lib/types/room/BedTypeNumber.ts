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
  bedSingle: number;
  bedDouble: number;
  bedQueen: number;
  bedKing: number;
  bedTwin: number;
  bedTriple: number;
}
