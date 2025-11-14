package org.example.angelbacked.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.angelbacked.entity.Diary;
import org.example.angelbacked.mapper.DiaryMapper;
import org.example.angelbacked.service.DiaryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryServiceImpl extends ServiceImpl<DiaryMapper, Diary> implements DiaryService {

    // 心情映射
    private static final Map<Integer, String[]> MOOD_MAP = new HashMap<>();

    static {
        MOOD_MAP.put(1, new String[]{"开心", "😊"});
        MOOD_MAP.put(2, new String[]{"平静", "😌"});
        MOOD_MAP.put(3, new String[]{"难过", "😢"});
        MOOD_MAP.put(4, new String[]{"愤怒", "😠"});
        MOOD_MAP.put(5, new String[]{"惊喜", "😲"});
        MOOD_MAP.put(6, new String[]{"焦虑", "😰"});
    }

    @Override
    public Diary saveDiary(Integer userId, String content, Integer moodId, Boolean isDraft, String tags, LocalDate date) {
        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setContent(content);
        diary.setMoodId(moodId);
        diary.setIsDraft(isDraft != null ? isDraft : false);
        diary.setTags(tags);
        // 如果提供了date参数，则使用它，否则使用创建时的日期
        if (date != null) {
            diary.setDate(date);
        } else {
            diary.setDate(LocalDate.now());
        }

        // 设置心情名称和图标
        if (MOOD_MAP.containsKey(moodId)) {
            String[] moodInfo = MOOD_MAP.get(moodId);
            diary.setMoodName(moodInfo[0]);
            diary.setMoodIcon(moodInfo[1]);
        }

        this.save(diary);
        return diary;
    }

    @Override
    public List<Diary> getRecentDiaries(Integer userId, Integer limit) {
        LambdaQueryWrapper<Diary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Diary::getUserId, userId)
                .orderByDesc(Diary::getCreatedAt);

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        return this.list(queryWrapper);
    }

    @Override
    public Map<Integer, Integer> getMoodStats(Integer userId) {
        Map<Integer, Integer> stats = new HashMap<>();
        // 初始化统计数据
        for (int i = 1; i <= 6; i++) {
            stats.put(i, 0);
        }

        LambdaQueryWrapper<Diary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Diary::getUserId, userId)
                .eq(Diary::getIsDraft, false); // 只统计非草稿日记

        List<Diary> diaries = this.list(queryWrapper);

        // 统计各类心情数量
        for (Diary diary : diaries) {
            Integer moodId = diary.getMoodId();
            if (moodId != null && moodId >= 1 && moodId <= 6) {
                stats.put(moodId, stats.get(moodId) + 1);
            }
        }

        return stats;
    }

    @Override
    public List<Diary> getDiariesByDate(Integer userId, String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        LambdaQueryWrapper<Diary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Diary::getUserId, userId)
                .eq(Diary::getDate, localDate)
                .orderByDesc(Diary::getCreatedAt);

        return this.list(queryWrapper);
    }

    @Override
    public Diary updateDiary(Integer userId, Integer diaryId, String content, Integer moodId, Boolean isDraft, String tags, LocalDate date) {
        Diary diary = this.getById(diaryId);

        // 验证日记是否存在且属于该用户
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new RuntimeException("日记不存在或无权限修改");
        }

        diary.setContent(content);
        diary.setMoodId(moodId);
        diary.setIsDraft(isDraft != null ? isDraft : false);
        diary.setTags(tags);
        // 如果提供了date参数，则更新date
        if (date != null) {
            diary.setDate(date);
        }

        // 设置心情名称和图标
        if (MOOD_MAP.containsKey(moodId)) {
            String[] moodInfo = MOOD_MAP.get(moodId);
            diary.setMoodName(moodInfo[0]);
            diary.setMoodIcon(moodInfo[1]);
        }

        this.updateById(diary);
        return diary;
    }

    @Override
    public boolean deleteDiary(Integer userId, Integer diaryId) {
        Diary diary = this.getById(diaryId);

        // 验证日记是否存在且属于该用户
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new RuntimeException("日记不存在或无权限删除");
        }

        return this.removeById(diaryId);
    }

    @Override
    public List<Diary> getDrafts(Integer userId) {
        LambdaQueryWrapper<Diary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Diary::getUserId, userId)
                .eq(Diary::getIsDraft, true)
                .orderByDesc(Diary::getCreatedAt);

        return this.list(queryWrapper);
    }
}