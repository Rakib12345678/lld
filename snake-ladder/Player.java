public class Player implements Observer {
    private String id;
    private String name;
    private int position;
    public Player(String id,String name){
        this.id = id;
        this.name = name;
        this.position = 0;
    }
    public void setPosition(int position){
        this.position = position;
    }
    public int getPosition(){
        return this.position;
    }
    public String getName(){
        return this.name;
    }
    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id = id;
    }

    @Override
    public void notify(String message) {
        System.out.println(name + ": " + message);
    }
}
