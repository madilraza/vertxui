package j2html;

public class UnescapedText extends DomContent {

    private String text;

    public UnescapedText(String text) {
        this.text = text;
    }

    @Override
    public String render() {
        return text;
    }

}
