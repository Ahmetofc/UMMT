package proj.AhmetRakap.db;

import java.time.LocalDateTime;

public class Message
{
    private String to, from, content;
    private LocalDateTime date;

    public Message(String _to, String _from, LocalDateTime _date, String _content)
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

    public void setDate(LocalDateTime _date)
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

    public LocalDateTime getDate()
    {
        return date;
    }

    public String getContent()
    {
        return content;
    }
}
