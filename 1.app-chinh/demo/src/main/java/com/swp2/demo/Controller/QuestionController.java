package com.swp2.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class QuestionController {

    // Inner class Question
    public static class Question {
        private final String questionText;
        private final List<String> options;

        public Question(String questionText, List<String> options) {
            this.questionText = questionText;
            this.options = options;
        }

        public String getQuestionText() {
            return questionText;
        }

        public List<String> getOptions() {
            return options;
        }

        public static List<Question> getDefaultQuestions() {
            return List.of(
                    new Question("How old are you?", List.of("Under 18", "18-24", "25-34", "35-44", "45+")),
                    new Question("How many cigarettes do you smoke per day?", List.of("None, I don't smoke daily", "1-5", "6-10", "11-20", "More than 20")),
                    new Question("At what age did you start smoking?", List.of("Under 16", "16-18", "19-25", "Over 25")),
                    new Question("How much money do you spend on cigarettes per week?", List.of("Less than 50,000 VND", "50,000 - 150,000 VND", "150,000 - 300,000 VND", "More than 300,000 VND")),
                    new Question("Have you tried to quit smoking before?", List.of("Yes, multiple times", "Yes, once", "No, I have never tried")),
                    new Question("Do you smoke more when you're stressed?", List.of("Yes, significantly more", "Yes, slightly more", "No, about the same", "I smoke less when stressed")),
                    new Question("Do you smoke indoors or outdoors?", List.of("Primarily indoors", "Primarily outdoors", "Both equally")),
                    new Question("Do you live with someone who also smokes?", List.of("Yes", "No"))
            );
        }

        public static List<String> extractAnswers(Map<String, String> formData) {
            List<String> answers = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                answers.add(formData.get("answer" + i));
            }
            return answers;
        }
    }

    // Inner class for result
    public static class AnalysisResult {
        public final String analysis;
        public final String recommendation;

        public AnalysisResult(String analysis, String recommendation) {
            this.analysis = analysis;
            this.recommendation = recommendation;
        }
    }

    // Logic phân tích
    public static AnalysisResult analyze(List<String> answers) {
        int score = 0;

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
                    "Based on your answers, your level of tobacco dependence is very high. This poses a serious risk to your health. We strongly recommend seeking professional support from our coach as soon as possible."
            );
        } else if (score >= 6) {
            return new AnalysisResult(
                    "Moderate Dependence Level",
                    "You show signs of tobacco dependence. Consider setting a clear quit plan and finding support early before your dependence deepens."
            );
        } else {
            return new AnalysisResult(
                    "Low Dependence Level",
                    "Your level of dependence appears low. This is a great opportunity to quit completely and protect your health for the long term."
            );
        }
    }

    // GET: hiển thị survey
    @GetMapping("/questionnaire")
    public String showSurveyForm(Model model) {
        model.addAttribute("questions", Question.getDefaultQuestions());
        return "question";
    }

    // POST: xử lý kết quả survey
    @PostMapping("/questionnaire")
    public String handleSurveySubmission(@RequestParam Map<String, String> formData, Model model) {
        List<String> answers = Question.extractAnswers(formData);
        AnalysisResult result = analyze(answers);

        model.addAttribute("questions", Question.getDefaultQuestions());
        model.addAttribute("answers", answers);
        model.addAttribute("analysisResult", result.analysis);
        model.addAttribute("recommendation", result.recommendation);

        return "result";
    }
}
