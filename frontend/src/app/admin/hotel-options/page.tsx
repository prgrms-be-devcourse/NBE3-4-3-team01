"use client";

import Loading from "@/components/hotellist/Loading";
import Navigation from "@/components/navigation/Navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  addHotelOption,
  deleteHotelOption,
  getAllHotelOptions,
  modifyHotelOption,
} from "@/lib/api/admin/HotelOptionApi";
import { OptionResponse } from "@/lib/types/admin/response/OptionResponse";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";

export default function HotelOptionsPage() {
  const [options, setOptions] = useState<OptionResponse[]>([]);
  const [editingOptionId, setEditingOptionId] = useState<number | null>(null);
  const [editingValues, setEditingValues] = useState<Record<number, string>>(
    {}
  );
  const [newOption, setNewOption] = useState<string>("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOptions();
  }, []);

  const fetchOptions = async () => {
    try {
      const data = await getAllHotelOptions();
      setOptions(data);
    } catch (error) {
      setError("호텔 데이터를 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const onModify = async (optionId: number) => {
    if (!editingValues[optionId] || !editingValues[optionId].trim()) return;

    try {
      await modifyHotelOption(optionId, { name: editingValues[optionId] });
      setEditingOptionId(null);
      setEditingValues((prev) => {
        const newValues = { ...prev };
        delete newValues[optionId];
        return newValues;
      });
      fetchOptions();
    } catch (error) {
      setError("옵션 수정 중 오류가 발생했습니다.");
    }
  };

  const onAddOption = async () => {
    if (!newOption.trim()) return;
    try {
      await addHotelOption({ name: newOption });
      setNewOption("");
      fetchOptions();
    } catch (error) {
      setError("옵션 추가 중 오류가 발생했습니다.");
  };

  const onDelete = async (optionId: number) => {
    if (!confirm("정말 이 옵션을 삭제하시겠습니까?")) return;
    try {
      await deleteHotelOption(optionId);
      fetchOptions();
    } catch (error: any) {
      const msg = error.response?.data?.msg;
      if (msg) {
        alert(msg);
      } else {
        setError("옵션 삭제 중 오류가 발생했습니다.");
      }
  };

  if (loading) return <Loading />;
  if (error) return <p className="text-center text-red-500">Error: {error}</p>;

  return (
    <div className="relative min-h-screen bg-background">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-blue-100 to-white" />

      {/* Decorative circles */}
      <div className="absolute top-20 right-20 w-64 h-64 bg-blue-200 rounded-full blur-3xl opacity-20" />
      <div className="absolute bottom-20 left-20 w-96 h-96 bg-blue-300 rounded-full blur-3xl opacity-10" />

      <div className="relative z-10">
        <Navigation />

        <div className="container mx-auto px-4 pt-40">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold text-gray-800 mb-4">옵션 관리</h1>
            <p className="text-lg text-gray-600">
              새로운 옵션을 추가하고 관리하세요
            </p>
          </div>

          <div className="max-w-4xl mx-auto">
            <Card className="bg-white/50 shadow-lg">
              <CardContent className="p-8">
                {/* 옵션 추가 박스 */}
                <div className="flex gap-3">
                  <div className="relative flex-1">
                    <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none">
                      <Plus size={20} />
                    </div>
                    <Input
                      type="text"
                      placeholder="새로운 옵션을 입력하세요"
                      value={newOption}
                      onChange={(e) => setNewOption(e.target.value)}
                      className="h-[52px] pl-12 pr-4 text-xl bg-white placeholder:text-xl"
                    />
                  </div>
                  <Button
                    onClick={onAddOption}
                    className="bg-blue-500 hover:bg-blue-600 text-white h-[52px] px-8 text-xl min-w-[120px]"
                  >
                    추가
                  </Button>
                </div>

                {/* 옵션 목록 */}
                <div className="mt-8 space-y-4">
                  {options.map((option) => (
                    <div
                      key={option.optionId}
                      className="bg-white p-4 rounded-lg shadow-sm flex items-center gap-3"
                    >
                      {editingOptionId === option.optionId ? (
                        <div className="relative flex-1">
                          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none">
                            <Pencil size={20} />
                          </div>
                          <Input
                            value={editingValues[option.optionId] ?? ""}
                            onChange={(e) =>
                              setEditingValues((prev) => ({
                                ...prev,
                                [option.optionId]: e.target.value,
                              }))
                            }
                            className="h-[42px] pl-10 pr-4 text-lg bg-white"
                          />
                        </div>
                      ) : (
                        <span className="flex-1 text-lg text-gray-700 pl-3">
                          {option.name}
                        </span>
                      )}

                      {editingOptionId === option.optionId ? (
                        <Button
                          onClick={() => onModify(option.optionId)}
                          className="bg-blue-500 hover:bg-blue-600 text-white h-[42px] px-6 text-lg"
                        >
                          저장
                        </Button>
                      ) : (
                        <Button
                          onClick={() => {
                            setEditingOptionId(option.optionId);
                            setEditingValues((prev) => ({
                              ...prev,
                              [option.optionId]: option.name,
                            }));
                          }}
                          variant="outline"
                          className="h-[42px] px-6 text-lg"
                        >
                          수정
                        </Button>
                      )}
                      {/* 삭제 버튼 */}
                      <Button
                        onClick={() => onDelete(option.optionId)}
                        variant="destructive"
                        className="h-[42px] px-6 text-lg"
                      >
                        삭제
                      </Button>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
