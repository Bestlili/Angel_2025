package org.example.angelbacked.controller;

import org.example.angelbacked.entity.Diary;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.service.DiaryService;
import org.example.angelbacked.util.JwtUtil;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private JwtUtil jwtUtil;

    // 从请求属性中获取当前用户
    private User getCurrentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }

    // 保存日记
    @PostMapping
    public Result saveDiary(@RequestBody DiaryRequest diaryRequest, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("未授权访问");
            }

            System.out.println("保存日记请求: 用户ID=" + currentUser.getId() + 
                             ", 内容=" + diaryRequest.getContent() + 
                             ", 心情ID=" + diaryRequest.getMoodId());

            Diary diary = diaryService.saveDiary(
                    currentUser.getId(),
                    diaryRequest.getContent(),
                    diaryRequest.getMoodId(),
                    diaryRequest.getIsDraft(),
                    diaryRequest.getTags(),
                    diaryRequest.getDate()
            );

            System.out.println("日记保存成功: ID=" + diary.getId());
            return Result.success("保存成功").setData(diary);
        } catch (Exception e) {
            System.out.println("保存日记失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 获取最近日记
    @GetMapping("/recent")
    public Result getRecentDiaries(@RequestParam(required = false, defaultValue = "10") Integer limit,
                                   HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("未授权访问");
            }

            List<Diary> diaries = diaryService.getRecentDiaries(currentUser.getId(), limit);
            return Result.success("获取成功").setData(diaries);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取心情统计
    @GetMapping("/stats")
    public Result getMoodStats(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("未授权访问");
            }

            Map<Integer, Integer> stats = diaryService.getMoodStats(currentUser.getId());
            return Result.success("获取成功").setData(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取特定日期日记
    @GetMapping
    public Result getDiariesByDate(@RequestParam String date, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("未授权访问");
            }

            List<Diary> diaries = diaryService.getDiariesByDate(currentUser.getId(), date);
            return Result.success("获取成功").setData(diaries);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 更新日记
    @PutMapping("/{id}")
    public Result updateDiary(@PathVariable Integer id,
                              @RequestBody DiaryRequest diaryRequest,
                              HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("未授权访问");
            }

            Diary diary = diaryService.updateDiary(
                    currentUser.getId(),
                    id,
                    diaryRequest.getContent(),
                    diaryRequest.getMoodId(),
                    diaryRequest.getIsDraft(),
                    diaryRequest.getTags(),
                    diaryRequest.getDate()
            );

            return Result.success("更新成功").setData(diary);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 删除日记
    @DeleteMapping("/{id}")
    public Result deleteDiary(@PathVariable Integer id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("未授权访问");
            }

            boolean result = diaryService.deleteDiary(currentUser.getId(), id);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取草稿列表
    @GetMapping("/drafts")
    public Result getDrafts(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return Result.error("未授权访问");
            }

            List<Diary> drafts = diaryService.getDrafts(currentUser.getId());
            return Result.success("获取成功").setData(drafts);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 日记请求数据类
    public static class DiaryRequest {
        private String content;
        private Integer moodId;
        private Boolean isDraft;
        private String tags;
        private LocalDate date;

        // Getters and Setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getMoodId() {
            return moodId;
        }

        public void setMoodId(Integer moodId) {
            this.moodId = moodId;
        }

        public Boolean getIsDraft() {
            return isDraft;
        }

        public void setIsDraft(Boolean isDraft) {
            this.isDraft = isDraft;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }
}