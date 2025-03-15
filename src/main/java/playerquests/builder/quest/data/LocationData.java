package playerquests.builder.quest.data;

import org.bukkit.Bukkit; // Bukkit API
import org.bukkit.Location; // defines a bukkit world location

/**
 * Represents a location in a three-dimensional space.
 */
public class LocationData {
    private String world; // The name of the world
    private double x; // The x-coordinate
    private double y; // The y-coordinate
    private double z; // The z-coordinate
    private float pitch; // The pitch rotation
    private float yaw; // The yaw rotation

    /**
     * Defaut constructor (for Jackson)
     */
    public LocationData() {}

    /**
     * Constructs a new Location with the specified parameters.
     *
     * @param world the name of the world
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param pitch the pitch rotation
     * @param yaw   the yaw rotation
     */
    public LocationData(String world, double x, double y, double z, float pitch, float yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    /**
     * Constructs a new Location with the specified parameters.
     *
     * @param location the location object to deep copy
     */
    public LocationData(LocationData location) {
        this.world = location.getWorld();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
    }

    /**
     * Returns the name of the world.
     *
     * @return the name of the world
     */
    public String getWorld() {
        return world;
    }

    /**
     * Sets the name of the world.
     *
     * @param world the name of the world
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     * Returns the x-coordinate.
     *
     * @return the x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x-coordinate.
     *
     * @param x the x-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the y-coordinate.
     *
     * @return the y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y-coordinate.
     *
     * @param y the y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the z-coordinate.
     *
     * @return the z-coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the z-coordinate.
     *
     * @param z the z-coordinate
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Returns the pitch rotation.
     *
     * @return the pitch rotation
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Sets the pitch rotation.
     *
     * @param pitch the pitch rotation
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Returns the yaw rotation.
     *
     * @return the yaw rotation
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Sets the yaw rotation.
     *
     * @param yaw the yaw rotation
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * Returns a string representation of the Location object.
     *
     * @return a string representation of the Location object
     */
    @Override
    public String toString() {
        return "Location{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}';
    }

    /**
     * Determines whether an NPC exists at the location specified.
     * @param otherLocation the location to check this LocationData against
     * @return whether or not there is a collision
     */
    public Boolean collidesWith(LocationData otherLocation) {
        // Check if each crucial property is equal
        return this.x == otherLocation.getX() &&
               this.y == otherLocation.getY() &&
               this.z == otherLocation.getZ() &&
               this.getWorld().equals(otherLocation.getWorld());
    }

    /**
     * Gets a normal bukkit location object from  
     * this PlayerQuests location object.
     * @return location of block represented in bukkit location format
     */
    public Location toBukkitLocation() {
        return new Location(
            Bukkit.getWorld(this.world), 
            this.x, 
            this.y, 
            this.z,
            this.yaw,
            this.pitch
        );
    }

    @Override
    public boolean equals(Object obj) {
        // if the object is not an instance of LocationData
        if (obj instanceof LocationData == false) {
            return false;
        }

        // cast to location data
        LocationData locationData = (LocationData) obj;

        return
            this.world.equals(locationData.world) &&
            this.x == locationData.x &&
            this.y == locationData.y &&
            this.z == locationData.z &&
            this.pitch == locationData.pitch &&
            this.yaw == locationData.yaw;
    }
}

