package au.gov.ga.hydroid.dto;

/**
 * The value of each key in the Json is an array, which is represented as Object by Gson Parser.
 * This is a utility object class which represents a value in the array.
 */
public class CmiSimpleJsonObject {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CmiSimpleJsonObject(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return "CmiSimpleJsonObject{" +
                "Value ='" + value + '\'' +
                '}';
    }
}
