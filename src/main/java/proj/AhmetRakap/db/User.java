package proj.AhmetRakap.db;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User
{
    private String username, password;
    private Boolean admin;
    private String firstName, lastName;
    private Character gender;
    private LocalDate birthday;
    private String email;

    public User(String _username, String _password, Boolean _admin, String _firstName, String _lastName, LocalDate _birthday, Character _gender, String _email)
    {
        username=_username;
        password=_password;
        admin=_admin;
        firstName=_firstName;
        lastName=_lastName;
        birthday=_birthday;
        gender=_gender;
        email=_email;
    }

    public User(String _username, String _password)
    {
        username=_username;
        password=_password;
    }

    public User(String _username)
    {
        username=_username;
    }

    public User()
    {
    }

    public void setUsername(String _username)
    {
        username=_username;
    }

    public void setPassword(String _password)
    {
        password=_password;
    }

    public void setAdmin(Boolean _admin)
    {
        admin=_admin;
    }

    public void setFirstName(String _firstName)
    {
        firstName=_firstName;
    }

    public void setLastName(String _lastName)
    {
        lastName=_lastName;
    }

    public void setBirthday(LocalDate _birthday)
    {
        birthday=_birthday;
    }

    public void setGender(Character _gender)
    {
        gender=_gender;
    }

    public void setEmail(String _email)
    {
        email=_email;
    }

    public String getUsername()
    {
       return username;
    }

    public String getPassword()
    {
        return password;
    }

    public Boolean getAdmin()
    {
        return admin;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public LocalDate getBirthday()
    {
        return birthday;
    }

    public Character getGender()
    {
        return gender;
    }

    public String getEmail()
    {
        return email;
    }
}
