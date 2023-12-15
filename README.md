# Dependencies
- Maven (mvn)
    - Current build command: ``mvn -f [path/to/plugin/root/directory] clean package -U -e``
- Java (openjdk-17)

# Specification
###### the way to visualise/think about, and implement the program.
Each <ins>quest</ins> is a <ins>container of events</ins>. Each <ins>event</ins> is a <ins>container of actions</ins> from the quest/NPC perspective (actions can also be stacked). Events are all the things which occur. See examples in the table:

| Action         | Events                                |
|----------------|---------------------------------------|
| Request Item   | Finding the flower                    |
| Speak          | First interaction with NPC            |
| Listen         | Deciding a favourite                  |
