import java.util.*;
import java.util.concurrent.RecursiveAction;
public class BasicDeckSim{
    protected HashSet<Integer> index= new HashSet<>(); //Tells the shuffle function what indexes have been assigned
    protected String[] deck; //The deck in its default state
    protected String[] temp; //The current state of the deck
    protected String[] p1= new String[5]; //Player 1 hand
    protected String[] p2= new String[5]; //Player 2 hand
    protected String[] p3= new String[5]; //Player 3 hand
    protected String[] p4= new String[5]; //Player 4 hand
    protected String[][] players= {p1,p2,p3,p4}; //Just here to make the draw function look neater.
    //boolean[] flags is here to help stop multiple threads from operating on the same position in the deck.
    protected boolean[] flags= {true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true};
    Random gen=new Random();
    public  BasicDeckSim(String[] deck2){
        deck= new String[deck2.length];
        temp= new String[deck2.length];
        
        for(int i=0;i<deck2.length;i++){
            deck[i]=deck2[i]; 
            temp[i]=deck2[i];
        }
    }
//shuffle() splits the array into 4 pieces, and creates threads that use different pieces.
    public int shuffleall(){
        int a=temp.length/4;
        shufflechild c1=new shufflechild(Arrays.copyOfRange(temp, a, 2*a),this);
        shufflechild c2=new shufflechild(Arrays.copyOfRange(temp, 2*a, 3*a),this);
        shufflechild c3=new shufflechild(Arrays.copyOfRange(temp, 3*a, temp.length),this);
        
        c1.fork();
        c2.fork();
        c3.fork();
        shuffle(Arrays.copyOfRange(temp,0,a));
        c1.join();
        c2.join();
        c3.join();
        return 1;

    }
//All threads check the index set at the same time and all threads add values to the temp array concurrently
    public int shuffle(String[] a){
        int in=-1;
        boolean flag=false;
        for(String x : a){
            while(!flag){
                in=gen.nextInt(temp.length);
                flag=index.add(in);
            }
            temp[in]=x;
            in=-1;
            flag=false;
        }
        return 1;
    }
    public int draw5(){
        if(deck.length<20)
             return 0;
        drawchild c1= new drawchild(1,this);
        drawchild c2= new drawchild(2,this);
        drawchild c3= new drawchild(3,this);
      
        c1.fork();
        c2.fork();  
        c3.fork();
        draw(0);
        c1.join();
        c2.join();
        c3.join();
        return 1;
    }

    public int draw(int arr){
        
        int in=0;
        for(int i=0;i<5;i++){
            //Avoiding race conditions is much harder for this function. That's where the flags array comes in.
            if(!flags[in]){
                while(!flags[in]){
                   in++;
                    }
                }
                flags[in]=false;
                try{ Thread.sleep(Long.valueOf(gen.nextInt(10)));}
                   catch(Exception e){return -1;}
            
            if(temp[in].equals("")){ //For extra redundancy
            i--;  
            }
            else{       
            players[arr][i]=temp[in];
            temp[in]="";
            try{ Thread.sleep(1);}
                   catch(Exception e){return -1;}
            }
            
            in++;

            

        }

        return 1;
    }
    //Restores the state of the object to its state immediately after initialization.
    public void reset(){
        temp=Arrays.copyOf(deck,deck.length);
        index.clear();
        for(String[] x: players){
            Arrays.fill(x,"");
        }
    }

    public void printAll(){
        System.out.println("P1: "+Arrays.toString(p1));
        System.out.println("P2: "+Arrays.toString(p2));
        System.out.println("P3: "+Arrays.toString(p3));
        System.out.println("P4: "+Arrays.toString(p4));
        System.out.println("P5: "+Arrays.toString(temp));
    }

    public String toString(){return Arrays.toString(temp);}


    public static void main(String[] args){
        String[] playingcards= new String[52];
        for(int i=0;i<52;i++){
            playingcards[i]=String.valueOf(i+1);
        }
        BasicDeckSim deck= new BasicDeckSim(playingcards);
        deck.shuffleall();
        System.out.println(deck.toString());
        deck.reset();
        System.out.println(deck.toString());
        deck.draw5();
        deck.printAll();
        deck.reset();;
        deck.printAll();
    }

    public static class shufflechild extends RecursiveAction{
        String[] subarray;
        BasicDeckSim deck;

        shufflechild(String[] a, BasicDeckSim d){ 
            subarray=new String[a.length];
            subarray=Arrays.copyOf(a, a.length);
            deck=d;
        }
        protected void compute(){ deck.shuffle(subarray); }
    }
    public static class drawchild extends RecursiveAction{
        int arr;
        BasicDeckSim deck;

        drawchild(int y, BasicDeckSim d){
            arr=y;
            deck=d;}
        protected void compute(){
            deck.draw(arr);
        }
    }

}