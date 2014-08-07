// Simplified Ethernet controller DMA layout model

/*
 * Abstract type: packet queue 
 */

// A packet fragment is an (address, size) tuple
type Fragment    = Tuple2[Int, Int]

// A packet consists of 1 or more fragments
type Packet      = List[Fragment]

type PacketQueue = List[Packet]

/* Abstract operations */

// Enqueue a packet
def absPush (q : PacketQueue, p : Packet) : PacketQueue = q :+ p

/* 
 * Concrete type: flattened list of buffer descriptors 
 */

// DMA descriptor stores a single packet fragment, as a four-tuple (address, size, last, own), 
// where _last_ is true for the last fragment of a packet, and _own_ is true if the descriptor
// is owned by the device.
type Descr = Tuple4[Int, Int, Boolean, Boolean]

// DMA circular buffer: (buffer, start index, end index)
type DMACB = (Vector[Descr], Int, Int)

/* Concrete mutators */

def setDescr (q : DMACB, i : Int, d : Descr) : DMACB = (q._1.updated(i, d), q._2, q._3)
def advanceLast  (q : DMACB, i : Int) : DMACB        = (q._1, q._2, (q._3 + i) % q._1.length)

/*
 * Semantic function: maps a concrete instance to an abstract one
 */

def sem(q : DMACB) : PacketQueue = doSem(q._1, q._2, q._2, Nil)

// Scan the input descriptor list until reaching either the end of 
// the list or a descriptor with own flag set to false.
def doSem(q : Vector[Descr], start : Int, idx : Int, frags : List[Fragment]) : PacketQueue = {
    val (addr, sz, lst, own) = q(idx)
    val pkt = frags :+ (addr, sz)
    val nxtidx = (idx + 1) % q.length
    assert (!own || nxtidx != start)
    (lst, own) match {
        case (true, true)  => pkt :: doSem(q, start, nxtidx, Nil)
        case (false, true) => doSem(q, start, nxtidx, pkt)
        case _             => assert (frags.isEmpty)
                              Nil
    }
}

def consistent (cb : DMACB) : Boolean = {
    val (q, start, end) = cb
    ((start != end) && q(start)._4 && consistent ((q, (start + 1) % q.length, end))) || 
    ((start == end) && !q(start)._4)
}


/*
 * Method to be synthesised (Leon-style spec)
 */
//def concPush (q : DMACB, p : Packet) : DMACB = {
//    require(!p.isEmpty);
//    require (consistent(q))
//    choose {
//        (res : DMACB) => (absPush (sem(q), p) == sem(res)) && consistent(res)
//    }
//}


/*
 * Expected synthesised implementation
 */
def concPush (q : DMACB, p : Packet) : DMACB = {
    require(!p.isEmpty, "empty packet");
    require(p.length < q._1.length - ((q._3 - q._2) % q._1.length), "packet too long");
    require (consistent(q), "queue in inconsistent state")
    val ((addr, len), idx) :: rest = p.zipWithIndex.reverse
    val q1 = setDescr (q, (q._3 + idx) % q._1.length, (addr, len, true, true))
    val q2 = rest.foldLeft(q1){(q, frag) => frag match {
                                                case ((addr, len), idx) => setDescr (q, (q._3 + idx) % q._1.length, (addr, len, false, true))
                                            }
                              }
    val res = advanceLast(q2, p.length)
    assert ((absPush (sem(q), p) == sem(res)) && consistent(res), "Postcondition of concPush does not hold: " + res + 
                                                                  "\n consistent=" + consistent(res) + 
                                                                  "\n sem = " + sem(res) + 
                                                                  "\n absPush = " + absPush (sem(q), p))
    res
} 

//    val q1 = q :+ (p._1, p._2, , false)
//    p.foreach(frag => );
//} 
//ensuring {(res : DMACB) => absPush (sem(q), p) = sem(res) }

/*
 * Test harness
 */

import scala.util.Random
import scala.math._

def genBuf(sz : Int, use : Int, rand : Random) : DMACB = {
    var (q:Vector[Descr]) = Vector()
    for (_ <- 0 to sz - 1) q = q :+ (rand.nextInt(), rand.nextInt(), rand.nextBoolean(), false)

    val start = rand.nextInt(sz)
    for (i <- 0 to use - 1) {
        val frag = (rand.nextInt(), rand.nextInt(), if (i == use - 1) true else rand.nextBoolean(), true)
        q = q.updated((start + i) % sz, frag)
    }
    (q, start, (start + use) % sz)
}

def genPkt(sz : Int, rand : Random) : Packet = {
    List(1 to sz).map (_ => (rand.nextInt(), rand.nextInt()))
}

def test (maxsz : Int, ntests : Int, rand : Random) = {
    for (i <- 0 to ntests - 1) {
        val sz   = rand.nextInt(maxsz-1) + 2
        val use  = rand.nextInt(sz - 1)
        val cb   = genBuf(sz, use, rand)
        val psz  = rand.nextInt(sz - use)
        val p    = genPkt (max(psz,1), rand)
        try {
            concPush(cb, p)
            println("test" + (i+1) + " passed")
        } catch {
            case e: AssertionError => println ("Failed test:")
                                      println (cb._1.mkString("\n"))
                                      println ("start = " + cb._2 + ", end = " + cb._3)
                                      println ("Packet: " ++ p)
                    throw e
        
        }
    }
}

test (32, 100000, Random)
