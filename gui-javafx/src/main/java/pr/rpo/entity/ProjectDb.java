package pr.rpo.entity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 控制逻辑 实体
 */
public class ProjectDb {

    private List<TitleAnswerInfo> data;
    //内部位置指针 初始为0
    private Integer loc;
    private Integer answerType;
    private Integer mode;

    public ProjectDb() {
        this.data = new ArrayList<TitleAnswerInfo>();
        this.loc = 0;
        this.mode = 1;
        this.answerType = 1;
    }

    public void chooseLoc(Integer loc) {
        if(loc <= 0 || loc >= data.size()) {
            ;
        }else {
            this.loc = loc;
        }
    }

    public void pre() {
        if(this.loc <= 0) {
            ;
        }else {
            this.loc--;
        }
    }

    public void next() {
        if(this.loc >= data.size()) {
            ;
        }else {
            this.loc++;
        }
    }

    // entry
    public void add(Integer titleIndex, String answer) {
        if(mode.equals(1)) {
            if(answerType == 1 || answerType == 2) {
                addChoiceAnswer(titleIndex, answer.chars().sorted().mapToObj(e -> (char) e).collect(Collectors.toSet()));
            }
        }else {
            if(answerType == 1 || answerType == 2) {
                correctChoiceAdd(titleIndex, answer.chars().sorted().mapToObj(e -> (char)e).collect(Collectors.toSet()));
            }
        }
    }

    private void addAnswer(Integer titleIndex, String answer) {
        addUnique(new TitleAnswerInfo(titleIndex, answer));
    }

    private void addChoiceAnswer(Integer titleIndex, Set<Character> choiceAnswer) {
        addUnique(new TitleAnswerInfo(titleIndex, choiceAnswer));
    }

    private void addUnique(TitleAnswerInfo titleAnswerInfo) {
        long existNum = data.stream().filter(e -> e.getTitleIndex().equals(titleAnswerInfo.getTitleIndex())).count();
        if(existNum >= 1) {
            data.remove(titleAnswerInfo);
            data.add(titleAnswerInfo);
        }else {
            data.add(titleAnswerInfo);
//            System.out.println(titleAnswerInfo.getTitleIndex() +":" +titleAnswerInfo.getAnswer());
        }
    }

    public void correctAdd(Integer titleIndex, String rightAnswer) {
        Iterator<TitleAnswerInfo> iterable = data.iterator();
        while(iterable.hasNext()) {
            TitleAnswerInfo t = iterable.next();
            if(t.getTitleIndex().equals(titleIndex)) {
                t.setRightAnswer(rightAnswer);
//                data.add(t);
                return;
            }
        }
    }

    public void correctChoiceAdd(Integer titleIndex, Set<Character> rightChoiceAnswer) {
        Iterator<TitleAnswerInfo> iterable = data.iterator();
        while(iterable.hasNext()) {
            TitleAnswerInfo t = iterable.next();
            if(t.getTitleIndex().equals(titleIndex)) {
                t.setRightChoiceAnswer(rightChoiceAnswer);
//                data.add(t);
                return;
            }
        }
    }

    public void clear() {
        data.clear();
        loc = 0;
    }

    public List<TitleAnswerInfo> getData() {
        return this.data;
    }

    public String getCurrentAnswer() {
        if(loc != data.size()) {
            return data.get(loc).getChoiceAnswerInString();
        }else {
            return "";
        }
    }

    public int getTitleIndex() {
        if(loc == data.size()) {
            if(loc == 0) {
                return 1;
            }else {
                return data.get(loc-1).getTitleIndex()+1;
            }
        }else {
            return data.get(loc).getTitleIndex();
        }
    }

    public int getLoc() {
        return this.loc;
    }

    public String getModeName() {
        if(mode == 1) {
            return "答题";
        }else {
            return "校正";
        }
    }

    public void triggerMode() {
        if(mode == 1) {
            mode = 2;
        }else {
            mode = 1;
        }
    }

    public void changeAnswerType() {
        //单选
        if(answerType == 1) {
            answerType = 2;
        }else {
            //多选
            answerType = 1;
        }
    }

    public String getAnswerTypeName() {
        if(answerType == 1) {
            return "单选";
        }else {
            return "多选";
        }
    }
}
