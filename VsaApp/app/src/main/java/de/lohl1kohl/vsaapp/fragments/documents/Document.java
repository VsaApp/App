package de.lohl1kohl.vsaapp.fragments.documents;

public class Document {
    private String url;
    private String text;
    private int groupID;
    private String groupName;

    public Document(String url, String text, int groupID, String groupName) {
        this.url = url;
        this.text = text;
        this.groupID = groupID;
        this.groupName = groupName;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    public int getGroupID() {
        return groupID;
    }

    public String getGroupName() {
        return groupName;
    }
}
