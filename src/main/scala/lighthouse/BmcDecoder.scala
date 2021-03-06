package lighthouse

import spinal.core._
import spinal.lib._
import _root_.lighthouse.lighthouse.ShiftCounter

class BmcDecoder(shortDelay: TimeNumber = 125 ns, 
                 unsyncDelay: TimeNumber = 250 ns) extends Component {
    val io = new Bundle {
        val signal = in Bool

        val output = master Flow(Bool)
        val sync = out Bool
    }

    assert(shortDelay.toBigDecimal < unsyncDelay.toBigDecimal)

    // Threshold value calculation. In case of non-exact result, the threshold
    // is floored
    val clockPeriod = ClockDomain.current.frequency.getValue.toTime
    val shortThreashold = (shortDelay / clockPeriod).toInt - 1
    val unsyncThreashold = (unsyncDelay / clockPeriod).toInt - 1

    val signal = RegNext(io.signal, init=False)
    val counter = new ShiftCounter(unsyncThreashold)

    val synchronized = RegInit(False)
    val decodingOne = RegInit(False)

    io.output.valid := False
    io.output.payload := False
    io.sync := synchronized

    when(counter === unsyncThreashold) {
        synchronized := False
    }

    when(signal.edge()) {
        counter.reset(0)

        when(!synchronized) {
            synchronized := True
            decodingOne := False
        }.otherwise {
            when(counter < shortThreashold) {
                when(decodingOne) {
                    io.output.payload := True
                    io.output.valid := True
                    decodingOne := False
                }.otherwise {
                    decodingOne := True
                }
            }.otherwise {
                io.output.payload := False
                io.output.valid := True
                decodingOne := False    // ToDo: check if we should unsync there!
            }
        }
    }.otherwise {
        counter.increment()
    }

}

import spinal.sim._
import spinal.core.sim._

object BmcDecoderSim {
  def main(args: Array[String]): Unit = {
    
    SimConfig.allOptimisation
            .addSimulatorFlag("-I../../sim_rtl")
            .withWave
            .compile (new BmcDecoder).doSim{ dut =>
      dut.clockDomain.forkStimulus(10)

      dut.io.signal #= false
      sleep(300)

      dut.clockDomain.waitEdge(8)
      dut.io.signal #= true
      dut.clockDomain.waitEdge(8)
      dut.io.signal #= false
      dut.clockDomain.waitEdge(8)
      dut.io.signal #= true
      dut.clockDomain.waitEdge(16)
      dut.io.signal #= false
      dut.clockDomain.waitEdge(8)
      dut.io.signal #= false
      dut.clockDomain.waitEdge(1)
      dut.io.signal #= true
      dut.clockDomain.waitEdge(7)
      dut.io.signal #= false
      dut.clockDomain.waitEdge(8)

      dut.clockDomain.waitRisingEdge(10)

      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= true
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(10)

      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= true
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(10)

      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= true
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= true
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(10)

      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= true
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= true
      dut.clockDomain.waitRisingEdge(1)
      dut.io.signal #= false
      dut.clockDomain.waitRisingEdge(10)
      



      sleep(100)

      simSuccess()
    }
  }
}