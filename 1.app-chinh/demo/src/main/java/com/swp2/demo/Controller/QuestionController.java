package com.swp2.demo.Controller;

import com.swp2.demo.entity.Question;
import com.swp2.demo.Repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    // GET: Hiển thị form câu hỏi từ database
    @GetMapping("/questionnaire")
    public String showSurveyForm(Model model) {
        List<Question> questions = questionRepository.findAll();
        model.addAttribute("questions", questions);
        return "question";  // Thymeleaf template question.html
    }

    // POST: Xử lý câu trả lời và hiển thị kết quả
    @PostMapping("/questionnaire")
    public String handleSurveySubmission(@RequestParam Map<String, String> formData, Model model) {
        List<String> answers = new ArrayList<>();
        for (int i = 0; i < formData.size(); i++) {
            answers.add(formData.get("answer" + i));
        }
        AnalysisResult result = analyze(answers);

        List<Question> questions = questionRepository.findAll();
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        model.addAttribute("analysisResult", result.analysis);
        model.addAttribute("recommendation", result.recommendation);

        return "result";  // Thymeleaf template result.html
    }

    // Class phân tích kết quả
    public static class AnalysisResult {
        public final String analysis;
        public final String recommendation;

        public AnalysisResult(String analysis, String recommendation) {
            this.analysis = analysis;
            this.recommendation = recommendation;
        }
    }

    // Phân tích điểm từ câu trả lời
    private static AnalysisResult analyze(List<String> answers) {
        int score = 0;
        if (answers.size() < 8) return new AnalysisResult("Invalid", "Not enough data to analyze.");

        String dailyCigarettes = answers.get(1);
        switch (dailyCigarettes) {
            case "1-5" -> score += 1;
            case "6-10" -> score += 2;
            case "11-20" -> score += 3;
            case "More than 20" -> score += 4;
        }

        String startAge = answers.get(2);
        switch (startAge) {
            case "Under 16" -> score += 2;
            case "16-18" -> score += 1;
        }

        String moneySpent = answers.get(3);
        switch (moneySpent) {
            case "50,000 - 150,000 VND" -> score += 1;
            case "150,000 - 300,000 VND" -> score += 2;
            case "More than 300,000 VND" -> score += 3;
        }

        String quitAttempts = answers.get(4);
        switch (quitAttempts) {
            case "Yes, multiple times" -> score += 2;
            case "Yes, once" -> score += 1;
        }

        String stressSmoking = answers.get(5);
        switch (stressSmoking) {
            case "Yes, significantly more" -> score += 2;
            case "Yes, slightly more" -> score += 1;
        }

        if ("Primarily indoors".equals(answers.get(6))) score += 1;
        if ("Yes".equals(answers.get(7))) score += 1;

        if (score >= 10) {
            return new AnalysisResult(
                    "Very High Dependence Level",
                    "Based on your answers, your level of tobacco dependence is very high. We strongly recommend seeking professional support as soon as possible."
            );
        } else if (score >= 6) {
            return new AnalysisResult(
                    "Moderate Dependence Level",
                    "You show signs of tobacco dependence. Consider finding support early before your dependence deepens."
            );
        } else {
            return new AnalysisResult(
                    "Low Dependence Level",
                    "Your level of dependence appears low. This is a great opportunity to quit completely and protect your health long-term."
            );
        }
    }
}
