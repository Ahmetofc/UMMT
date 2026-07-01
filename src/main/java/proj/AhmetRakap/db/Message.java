package proj.AhmetRakap.db;

public class Message
{
    private String to, from, date, content;

    public Message(String _to, String _from, String _date, String _content)
    {
        to=_to;
        from=_from;
        date=_date;
        content=_content;
    }

    public void setTo(String _to)
    {
        to=_to;
    }

    public void setFrom(String _from)
    {
        from=_from;
    }

    public void setDate(String _date)
    {
        date=_date;
    }

    public void setContent(String _content)
    {
        content=_content;
    }

    public String getTo()
    {
        return to;
    }

    public String getFrom()
    {
        return from;
    }

    public String getDate()
    {
        return date;
    }

    public String getContent()
    {
        return content;
    }
}
