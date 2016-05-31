/**
 * Created by tylerrozboril on 5/31/16.
 */
public class Info {
    private String name;
    private int owner_id;
    private String description;
    private String address;
    private String city;
    private String state;
    private int zip;
    private String phone_number;

    public Info(String formData){
        String[] parts = formData.split("&");
        String theOwner = parts[0];
        String name = parts[1];
        String phone_number = parts[2];
        String address = parts[3];
        String city = parts[4];
        String state = parts[5];
        String theZip = parts[6];
        String description = parts[7];

        owner_id = Integer.parseInt(theOwner);
        zip = Integer.parseInt(theZip);
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(int owner_id) {
        this.owner_id = owner_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getZip() {
        return zip;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}
