// Simplified Ethernet controller DMA layout model

/*
 * Abstract type: packet queue 
 */

// A packet fragment
case class Fragment(addr : Int, size : Int)

// A packet consists of 1 or more fragments
type Packet      = List[Fragment]

type PacketQueue = List[Packet]

/* Abstract operations */

// Enqueue a packet
def absPush (q : PacketQueue, p : Packet) : PacketQueue = q :+ p

/* 
 * Concrete type: flattened list of buffer descriptors 
 */

// DMA descriptor stores a single packet fragment
// _islast_ is true for the last fragment of a packet, and _own_ is true if the descriptor
// is owned by the device.
case class Descr(addr : Int, size : Int, islast : Boolean, own : Boolean)

// DMA circular buffer: (buffer, start index, end index)
case class DMACB (buf : Vector[Descr], start : Int, end : Int)

/* Concrete mutators */

def setDescr (q : DMACB, i : Int, d : Descr) : DMACB = DMACB(q.buf.updated(i, d), q.start, q.end)
def advanceLast  (q : DMACB, i : Int) : DMACB        = DMACB(q.buf, q.start, (q.end + i) % q.buf.length)

/*
 * Semantic function: maps a concrete instance to an abstract one
 */

def sem(q : DMACB) : PacketQueue = doSem(q.buf, q.start, q.start, Nil)

// Scan the input descriptor list until reaching either the end of 
// the list or a descriptor with own flag set to false.
def doSem(q : Vector[Descr], start : Int, idx : Int, frags : List[Fragment]) : PacketQueue = {
    val Descr(addr, sz, lst, own) = q(idx)
    val pkt = frags :+ Fragment(addr, sz)
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
    ((cb.start != cb.end) && cb.buf(cb.start).own && consistent (DMACB(cb.buf, (cb.start + 1) % cb.buf.length, cb.end))) || 
    ((cb.start == cb.end) && !cb.buf(cb.start).own)
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
    require(p.length < q.buf.length - ((q.end - q.start) % q.buf.length), "packet too long");
    require (consistent(q), "queue in inconsistent state")
    val (Fragment(addr, len), idx) :: rest = p.zipWithIndex.reverse
    val q1 = setDescr (q, (q.end + idx) % q.buf.length, Descr(addr, len, true, true))
    val q2 = rest.foldLeft(q1){(q, frag) => frag match {
                                                case (Fragment(addr, len), idx) => setDescr (q, (q.end + idx) % q.buf.length, Descr(addr, len, false, true))
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
    for (_ <- 0 to sz - 1) q = q :+ Descr(rand.nextInt(), rand.nextInt(), rand.nextBoolean(), false)

    val start = rand.nextInt(sz)
    for (i <- 0 to use - 1) {
        val frag = Descr(rand.nextInt(), rand.nextInt(), if (i == use - 1) true else rand.nextBoolean(), true)
        q = q.updated((start + i) % sz, frag)
    }
    DMACB(q, start, (start + use) % sz)
}

def genPkt(sz : Int, rand : Random) : Packet = {
    List(1 to sz).map (_ => Fragment(rand.nextInt(), rand.nextInt()))
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
                                      println (cb.buf.mkString("\n"))
                                      println ("start = " + cb.start + ", end = " + cb.end)
                                      println ("Packet: " ++ p)
                    throw e
        
        }
    }
}

test (32, 100000, Random)
