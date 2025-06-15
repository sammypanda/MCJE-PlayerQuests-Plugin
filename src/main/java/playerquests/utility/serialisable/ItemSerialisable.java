package playerquests.utility.serialisable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ItemSerialisable implements Serialisable {

    @JsonIgnore
    private Material material;

    @JsonIgnore
    private PotionType potionType;

    public ItemSerialisable(String string) {
        this.fromString(string);
    }

    public ItemSerialisable(ItemStack itemStack) {
        this.fromItemStack(itemStack);
    }

    /**
     * Takes an ItemSerialisable or Material string and converts it
     * to an ItemSerialisable object.
     * @param serialised an ItemSerialisable or Material string
     * @return deserialised ItemSerialisable object
     */
    public ItemSerialisable fromString(String serialised) {
        if (serialised == null || serialised.isEmpty()) {
            throw new IllegalArgumentException("Serialized string cannot be null or empty");
        }

        // get map of key value pairs of the attributes
        Map<String, String> data = Arrays.stream(serialised.split(","))
            .map(keyvalue -> keyvalue.split(":", 2))
            .collect(Collectors.toMap(
                keyvalue -> keyvalue[0],
                keyvalue -> keyvalue.length > 1 ? keyvalue[1] : ""
            ));

        // Get material
        this.findMaterial(data.get("material"), serialised);

        if (this.material.equals(Material.POTION)) {
            // Get potion stuff
            this.findPotion(data.get("potion_type"));
        }

        return this;
    }

    private void findPotion(String potion_type) {
        try {
            this.potionType = PotionType.valueOf(potion_type);
        } catch (Exception e) {
            this.potionType = PotionType.AWKWARD;
        }
	}

	private void findPotion(ItemStack itemStack) {
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

        // set potion type
        this.potionType = potionMeta.getBasePotionType();
	}

	private void findMaterial(String materialString, String serialisedString) {
        if (materialString == null) {
            materialString = serialisedString; // for if the string passed in is a raw material
        }

        // resolve material from string
        try {
            this.material = Material.valueOf(materialString.toUpperCase());
        } catch (IllegalArgumentException _e) {
            throw new IllegalArgumentException("Invalid material encountered");
        }
    }

    public ItemSerialisable fromItemStack(ItemStack itemStack) {
        // get material from itemstack
        this.material = itemStack.getType();

        // get potion details from itemstack
        if (this.material == Material.POTION) {
            this.findPotion(itemStack);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemSerialisable that = (ItemSerialisable) o;
        return Objects.equals(this.toString(), that.toString()); // TODO: make more robust/manually checking the fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString()); // TODO: make more robust/hashing the fields
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // set base material
        sb.append("material:").append(this.getMaterial());

        // add new unique attributes here:
        appendIfNotNull(sb, ",potion_type:", this.getPotionType());

        return sb.toString();
    }

    // Helper method
    private void appendIfNotNull(StringBuilder sb, String prefix, Object value) {
        if (value != null) sb.append(prefix).append(value);
    }

    public ItemStack toItemStack() {
        Material material = this.getMaterial();
        ItemStack itemStack = new ItemStack(material);

        if (material.equals(Material.POTION)) {
            this.findPotion(itemStack);
        }

        return itemStack;
    }

	public Material getMaterial() {
	    if (this.material == null) {
			throw new IllegalStateException("ItemSerialisable missing a material");
		}

	    return this.material;
	}

	public PotionType getPotionType() {
	    return this.potionType;
	}
}
