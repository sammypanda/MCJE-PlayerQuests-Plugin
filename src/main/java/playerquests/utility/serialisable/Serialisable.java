package playerquests.utility.serialisable;

public interface Serialisable {
    @Override
    String toString();

    Serialisable fromString(String string);
}
