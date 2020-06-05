import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.*;

public class Factory{
    public static void main(String[] args) {
        ExecutorService factory = Executors.newCachedThreadPool();
        LinkedBlockingQueue<can>rack = new LinkedBlockingQueue<>(4);
        LinkedBlockingQueue<can>fill = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<can>seal = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<can>sealingrack = new LinkedBlockingQueue<>(6);
        LinkedBlockingQueue<can>label = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<can>scanbelt = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<can>canpackage = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<can>packagerack = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<can>reject = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<packages>boxing = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<packages>boxingpackage = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<boxes>storing = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<boxes>deliver = new LinkedBlockingQueue<>();
        ScheduledExecutorService ex = Executors.newScheduledThreadPool(3);
        bays b = new bays(2);
        for (int i=1;i<=15000;i++){
            can c = new can(i);
            System.out.println("Factory has received a new can: " + i + "....sending to scan for defects.");
            Future<can> scan = factory.submit(new scan(c, storing));
            try{
                Thread.sleep(100);
                steriles s = new steriles(rack, fill, scan.get());
                new Thread(s).start(); //test
                filling f = new filling(fill, seal, c);
                new Thread(f).start();
                sealing ss = new sealing(seal, scanbelt, sealingrack, c);
                new Thread(ss).start();
                labeling l = new labeling(scanbelt, label, canpackage, c);
                new Thread(l).start();
                packaging p = new packaging(canpackage, packagerack, boxing, c);
                new Thread(p).start();
                boxingp bp = new boxingp(boxing, boxingpackage,storing);
                new Thread(bp).start();
                for(int k=1 ; k<4; k++){
                delivering dl = new delivering(b, k ,storing, deliver);
                ex.scheduleAtFixedRate(new Thread(dl), 0, 1, TimeUnit.SECONDS);
                }
            }catch(Exception e){}
        }
        System.out.println("Cans are all done.");
    }
}

class can{
    int canid;
    boolean isSterile = false;
    public can(int canid){
        this.canid = canid;
    }
    public int getID(){
        return canid;
    }
}

class packages{
    int pid;
    ArrayList<can>pack;
    public packages(int pid, ArrayList<can>pack){
        this.pid = pid;
        this.pack = pack;
    }
    public int getID(){
        return pid;
    }
}

class boxes{
    int boxid;
    ArrayList<packages>box;
    public boxes(int boxid, ArrayList<packages>box){
        this.boxid = boxid;
        this.box = box;
    }
    public int getID(){
        return boxid;
    }
}

class forklifts{
    int fid;
    //ArrayList<boxes>deliver;
    boolean working = true;
    public forklifts(int fid){
        this.fid = fid;
       // this.deliver = deliver;
    }
}

class bays{
    Semaphore s;
    public bays(int size){
        s = new Semaphore(size);
    }
    public void enter(int id){
        System.out.println("Van " + id + " is entering.");
        try{
            s.acquire();
            System.out.println("Van " + id + " has occupy a bay. "
                    + s.availablePermits() + " bay(s) available.");
        }catch(Exception e){}
    }
    public void exit(int id){
        System.out.println("Van " + id + " is leaving the bay.");
        s.release();
    }
}

class vans{
    int vid;
   // ArrayList<boxes>item;
    public vans(int vid){
        this.vid = vid;
     //   this.item = item;
    }
}

class scan implements Callable<can>{    
    can c;
    Random rand = new Random();
    LinkedBlockingQueue<can>reject;
    LinkedBlockingQueue<boxes>storing = new LinkedBlockingQueue<>();
    int r = rand.nextInt(10);
    
    public scan(can c, LinkedBlockingQueue<boxes>storing){
        this.c = c;
        this.storing = storing;
    }

    @Override
    public can call() throws Exception{
        if(storing.size()==12){
            try{
                wait();
            }catch(Exception e){}
        }
        System.out.println("SCAN: Scanning cans....");
        try{
            Thread.sleep(500);
        }catch(Exception e){}
        if(r == 5){
          c.isSterile = false;  
          System.out.println("SCAN: Can " + c.canid + " is defective. Unaccepted.");
          reject.add(c);
        }else{
        c.isSterile = true;
        System.out.println("SCAN: Can " + c.canid + " has no defects. Accepted.");
        return c;
        }
        return null;
    }    
}
class steriles implements Runnable{
    LinkedBlockingQueue<can>rack;
    LinkedBlockingQueue<can>fill;
    can c;
    int cid;
    public steriles(LinkedBlockingQueue<can>rack, LinkedBlockingQueue<can>fill, can c){
        this.rack = rack;
        this.fill = fill;
        this.c = c;
        this.cid = this.c.canid;
    }
    public int rack(){
        return rack.size();
    }
    public void run(){
        if(true){
            addCan();
            try{
                Thread.sleep(100);
            }catch(Exception e){}
        }
    }
    synchronized public void addCan(){
            if(rack.size()==4){
                System.out.println("\tRACK: Rack is full. Preparing for sterilising.");
                try{
                    Thread.sleep(500);
                    System.out.println("\tRACK: Sterilising complete. Removing cans from rack...");
                }catch(Exception e){}
                while(rack.size()!=0){
                    try{
                        fill.put(rack.take());
                        notify(); 
                    }catch(Exception e){}
                }
            }else{
            try{
                rack.put(c);            
                System.out.println("\tRACK: Added can " + cid + " to the rack.");
            }catch(Exception e){}
            }
    }
}

class filling implements Runnable{
    LinkedBlockingQueue<can>fill;
    LinkedBlockingQueue<can>seal;
    LinkedBlockingQueue<can>reject;
    can c;
    int cid;
    Random rand = new Random();
    int r = rand.nextInt(10);
    
    public filling(LinkedBlockingQueue<can>fill, LinkedBlockingQueue<can>seal, can c){
        this.fill = fill;
        this.seal = seal;
        this.c = c ;
        this.cid = this.c.canid;
    }
        public int fill(){
        return fill.size();
    }
    
    public void run(){
        if(true){
            fillCan();
            try{
                Thread.sleep(500);
            }catch(Exception e){}
        }
    }
    synchronized public void fillCan(){
        while(fill.size()<1){
            try{
                wait();
            }catch(Exception e){}
        }
        try{
        can fc = fill.take();
        System.out.println("\t\tFILL: Filling into can " + fc.canid + "....");
        Thread.sleep(1500);
        System.out.println("\t\tFILL: Filling is done for can " + fc.canid + ". Scanning for errors. ");
           // System.out.println("Cans in queue: " + fill);
        Thread.sleep(1500);
        if(r==5){
            System.out.println("\t\tFILL: Can " + fc.canid + " is not filled properly. Discarding...");
            reject.add(fc);
        }else{
            System.out.println("\t\tFILL: Can " + fc.canid + " is filled properly. Proceeding to seal.");
            seal.put(fc);
            notify();
        }
        }catch(Exception e){}
    }
}

class sealing implements Runnable{
    LinkedBlockingQueue<can>seal;
    LinkedBlockingQueue<can>sealingrack;
    LinkedBlockingQueue<can>scanbelt;
    can c;
    int cid;
    
    public sealing(LinkedBlockingQueue<can>seal, LinkedBlockingQueue<can>scanbelt, LinkedBlockingQueue<can>sealingrack, can c){
        this.seal = seal;
        this.sealingrack = sealingrack;
        this.scanbelt = scanbelt;
        this.c = c;
        this.cid = this.c.canid;
    }
    public int rack(){
        return sealingrack.size();
    }
    public void run(){
        if(true){
            addCan();
            try{
                Thread.sleep(2000);
            }catch(Exception e){}
        }
    }
    synchronized public void addCan(){
        if(seal.size()<1){
            try{
                wait();
            }catch(Exception e){}
        }
            if(sealingrack.size()==12){
                System.out.println("\t\t\tSEAL RACK: Sealing rack is full. Preparing for sealing.");
                try{
                    Thread.sleep(1500);
                    System.out.println("\t\t\tSEAL RACK: Sealing complete. Removing from sealing rack.");
                    while(sealingrack.size()!=0){
                        scanbelt.put(sealingrack.take());
                        notify();
                    }
                    Thread.sleep(2000);
                }catch(Exception e){}
            }
            try{           
                can sc = seal.take();
                System.out.println("\t\t\tSEAL RACK: Added can " + sc.canid + " to the sealing rack.");
                sealingrack.put(sc);
                Thread.sleep(2500);
            }catch(Exception e){}
    }
}

class labeling implements Runnable{
    LinkedBlockingQueue<can>label;
    LinkedBlockingQueue<can>scanbelt;
    LinkedBlockingQueue<can>canpackage;
    LinkedBlockingQueue<can>reject;
    can c;
    int cid;
    Random rand = new Random();
    int r = rand.nextInt(10);
    
    public labeling(LinkedBlockingQueue<can>scanbelt, LinkedBlockingQueue<can>label, LinkedBlockingQueue<can>canpackage, can c){
        this.label = label;
        this.canpackage = canpackage;
        this.scanbelt = scanbelt;
        this.c = c;
        this.cid = this.c.canid;
    }
    public int rack(){
        return label.size();
    }
    public void run(){
        if(true){
            scanCan();
            try{
                Thread.sleep(2000);
            }catch(Exception e){}
        }
    }
    synchronized public void scanCan(){
        if(scanbelt.size()<1){
            try{
                wait();
            }catch(Exception e){}
        }
            try{           
                can sc = scanbelt.take();
                System.out.println("\t\t\t\tSCAN BELT: Scanning can " + sc.canid);
                Thread.sleep(1500);
                if(r == 5){
                    System.out.println("\t\t\t\tSCAN BELT: Can " + sc.canid + " is not sealed properly. Removing....");
                    reject.add(sc);
                }else{
                    System.out.println("\t\t\t\tSCAN BELT: Can " + sc.canid + " is sealed properly. Proceeding to label.");
                    label.put(sc);
                    Thread.sleep(1500);                    
                }
            }catch(Exception e){}
            try{
                can labelcan = label.take();
                System.out.println("LABEL: Labeling can " + labelcan.canid + "....");
                Thread.sleep(1000);
                System.out.println("LABEL: Labeling done. Scanning can " + labelcan.canid + ".....");
                if(r == 5){
                    System.out.println("LABEL: Can " + labelcan.canid + " is not labelled properly. Discarding...");
                }else{
                    System.out.println("LABEL: Can " + labelcan.canid + " is labelled properly. Moving to packaging....");
                    canpackage.put(labelcan);
                }
            }catch(Exception e){}
    }
}

class packaging implements Runnable{
    LinkedBlockingQueue<can>canpackage;
    LinkedBlockingQueue<can>packagerack;
    LinkedBlockingQueue<packages>boxing;
    ArrayList<can>pack = new ArrayList<>();
    can c;
    int cid;
    static int packid=1;
    
    public packaging(LinkedBlockingQueue<can>canpackage, LinkedBlockingQueue<can>packagerack, LinkedBlockingQueue<packages>boxing, can c){
        this.canpackage = canpackage;
        this.packagerack = packagerack;
        this.boxing = boxing;
        this.c = c;
        this.cid = this.c.canid;
    }
    public int rack(){
        return packagerack.size();
    }
    public void run(){
        if(true){
            addCan();
            try{
                Thread.sleep(1000);
            }catch(Exception e){}
        }
    }
    synchronized public void addCan(){
        if(canpackage.size()<2){
            try{
                wait();
            }catch(Exception e){}
        }
        packages p = new packages(packid, pack);
        if(canpackage.size()==6){
            while(!canpackage.isEmpty()){
            try{        
                System.out.println("\tPACKAGE: Added can " + canpackage.peek().canid + " to the packaging rack.");
                p.pack.add(canpackage.take());    
                Thread.sleep(1000);
                notify();
                }catch(Exception e){}
            }
            //String[] strObjects = pack.toArray(new String[0]);
            System.out.println("CANS IN THE PACKAGE  " + packid + " : " + pack);
            packid++;
            try{
                boxing.put(p);  
                pack.removeAll(pack);
                notify();
                Thread.sleep(1000);
            }catch(Exception e){}
        }
    }
}
class boxingp implements Runnable{
    LinkedBlockingQueue<packages>boxing;
    LinkedBlockingQueue<packages>boxingpackage;
    LinkedBlockingQueue<boxes>storing;
    ArrayList<packages>box = new ArrayList<>(27);    
    static int boxid=1;
    
    public boxingp(LinkedBlockingQueue<packages>boxing, LinkedBlockingQueue<packages>boxingpackage, LinkedBlockingQueue<boxes>storing){
        this.boxingpackage = boxingpackage;
        this.storing = storing;
        this.boxing = boxing;
    }
    public int rack(){
        return boxing.size();
    }
    public void run(){
        if(true){
            addBox();
            try{
                Thread.sleep(1000);
            }catch(Exception e){}
        }
    }
    synchronized public void addBox(){
        boxes b = new boxes(boxid, box);
        while(boxing.size()<1){
            try{
                wait();
            }catch(Exception e){}
        }
        if(boxing.size() == 27){
            System.out.println("\tBOX: Box is full.");
            while(boxing.size() != 0){
                try{
                    b.box.add(boxing.take());
                    notify();
                    Thread.sleep(1000);                    
                }catch(Exception e){}
            }
            for(int j = 0; j < box.size() ; j++){
                System.out.println("\tBox " + boxid + " is moving to the loading area with packages " + box.get(j).getID());
            }
            System.out.println("PACKAGES IN THE BOX  " + boxid + " : " + box);
            boxid++;
            try{
                    storing.put(b);  
                    box.removeAll(box);      
                    notify();
                    Thread.sleep(1000);
            }catch(Exception e){}
        }
    }
}

class delivering implements Runnable{
    LinkedBlockingQueue<boxes>storing;
    LinkedBlockingQueue<boxes>deliver;
    Random rand = new Random();
    int assign = rand.nextInt(3);
    Random rand2 = new Random();
    int defect = rand.nextInt(2);
    bays b;
    int vid;
    
    public delivering(bays b, int vid, LinkedBlockingQueue<boxes>storing,LinkedBlockingQueue<boxes>deliver){
        this.storing = storing;
        this.deliver = deliver;
        this.b = b;
        this.vid = vid;
    }
    
    public void run(){
        try{
            if(b.s.availablePermits()!=0){
            b.enter(vid);
            }
            addBox();
            Thread.sleep(1000);
        }catch(Exception e){}
        
    }
    
    synchronized public void addBox(){
        vans v = new vans(vid);
        if(storing.size()<1){
            try{
               // wait();
            }catch(Exception e){}
        }
        if(storing.size()==2){
            try{
                if(assign == 0){
                    if(defect == 0){
                        System.out.println("Forklift one is defective. Repairing.....");
                        Thread.sleep(10000);
                    }
                System.out.println("Forklift one is moving box " + storing.peek().boxid + " to the loading bay.");
                deliver.put(storing.take());
                notifyAll();
                }else if(assign == 1){
                    if(defect == 0){
                        System.out.println("Forklift two is defective. Repairing.....");
                        Thread.sleep(10000);
                    }
                System.out.println("Forklift two is moving box " + storing.peek().boxid + " to the loading bay.");
                deliver.put(storing.take());
                notifyAll();
                }else if(assign == 2){
                    if(defect == 0){
                        System.out.println("Forklift three is defective. Repairing.....");
                        Thread.sleep(10000);
                    }
                System.out.println("Forklift three is moving box " + storing.peek().boxid + " to the loading bay.");
                deliver.put(storing.take());
                notifyAll();
                }else{
                    System.out.println("Error occured. Waiting for the forklifts to be ready...");
                } 
            }catch(Exception e){}
        }
        if(deliver.size()==20){
            b.exit(vid);
            System.out.println("Van " + vid + " is delivering boxes: " + deliver);
            try{
            deliver.removeAll(deliver);
            Thread.sleep(30000000);
            }catch(Exception e){}
        }
    }
}
