package org.example.angelbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.angelbacked.entity.Diary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DiaryService extends IService<Diary> {
    // 保存日记
    Diary saveDiary(Integer userId, String content, Integer moodId, Boolean isDraft, String tags, LocalDate date);

    // 获取用户最近的日记
    List<Diary> getRecentDiaries(Integer userId, Integer limit);

    // 获取用户心情统计
    Map<Integer, Integer> getMoodStats(Integer userId);

    // 获取特定日期的日记
    List<Diary> getDiariesByDate(Integer userId, String date);

    // 更新日记
    Diary updateDiary(Integer userId, Integer diaryId, String content, Integer moodId, Boolean isDraft, String tags, LocalDate date);

    // 删除日记
    boolean deleteDiary(Integer userId, Integer diaryId);

    // 获取用户的草稿列表
    List<Diary> getDrafts(Integer userId);
}