package pr.rpo.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 题目回答实体
 * 涵盖单选题、多选题、简答题
 */
public class TitleAnswerInfo implements Comparable {

    /**
     * 题目类别
     */
    private String title;
    
    /**
     * 题目序号
     */
    private Integer titleIndex;
    
    /**
     * 回答
     */
    private String answer;

    /**
     * 正确回答
     */
    private String rightAnswer;
    
    /**
     * 选择题回答
     */
    private Set<Character> choiceAnswer = new HashSet<>();
    
    /**
     * 选择题答案
     */
    private Set<Character> rightChoiceAnswer = new HashSet<>();
   
    /**
     * 回答开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 回答结束时间
     */
    private LocalDateTime endTime;

    /**
     * 答题耗时
     */
    private Duration duration;

    public static long getDuration(TitleAnswerInfo titleAnswerInfo) {
        return Duration.between(titleAnswerInfo.getStartTime(), titleAnswerInfo.getEndTime()).getSeconds();
    }

    public TitleAnswerInfo(Integer titleIndex, String answer) {
        this.titleIndex = titleIndex;
        this.answer = answer;
    }

    public TitleAnswerInfo(Integer titleIndex, Character choiceAnswer) {
        this.titleIndex = titleIndex;
        this.choiceAnswer.add(choiceAnswer);
    }

    public TitleAnswerInfo(Integer titleIndex, Set<Character> choiceAnswer) {
        this.titleIndex = titleIndex;
        this.choiceAnswer = choiceAnswer;
    }

    public TitleAnswerInfo(Integer titleIndex, String answer, LocalDateTime answerBeginTime, LocalDateTime answerFinishTime) {
        this.titleIndex = titleIndex;
        this.answer = answer;
        this.startTime = answerBeginTime;
        this.endTime = answerFinishTime;
    }

    public TitleAnswerInfo(Integer titleIndex, String answer, LocalDateTime answerBeginTime, Duration duration) {
        this.titleIndex = titleIndex;
        this.answer = answer;
        this.startTime = answerBeginTime;
        this.duration = duration;
    }

    public Integer getTitleIndex() {
        return titleIndex;
    }

    public void setTitleIndex(Integer titleIndex) {
        this.titleIndex = titleIndex;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(String rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    
    
    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Set<Character> getChoiceAnswer() {
        return choiceAnswer;
    }

    public String getChoiceAnswerInString() {
        StringBuilder sb = new StringBuilder();
        choiceAnswer.stream().sorted().forEach(e -> {
            sb.append(e);
        });
        return sb.toString();
    }

    public void setChoiceAnswer(Set<Character> choiceAnswer) {
        this.choiceAnswer = choiceAnswer;
    }

    public Set<Character> getRightChoiceAnswer() {
        return rightChoiceAnswer;
    }

    public String getRightChoiceAnswerInString() {
        StringBuilder sb = new StringBuilder();
        rightChoiceAnswer.stream().sorted().forEach(e -> {
            sb.append(e);
        });
        return sb.toString();
    }

    public void setRightChoiceAnswer(Set<Character> rightChoiceAnswer) {
        this.rightChoiceAnswer = rightChoiceAnswer;
    }

    @Override
    public int compareTo(Object o) {
        return this.getTitleIndex().compareTo(((TitleAnswerInfo)o).getTitleIndex());
    }

    @Override
    public boolean equals(Object object) {
        if(this.getTitleIndex().equals(((TitleAnswerInfo) object).getTitleIndex())) {
            return true;
        }else {
            return false;
        }
    }
}
