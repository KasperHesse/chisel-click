package click

import chisel3._
import chiseltest._

object HandshakeTesting {
  /**
    Implicit classes allow adding additional functionality to existing classes.
    The [[HandshakeDriver]] extends the [[ReqAck]] bundle with send and receive
    function which can be used in chiseltest testbenches. The implementation
    follows along the [[chiseltest.DecoupledDriver]].
   */
  implicit class HandshakeDriver[T <: Data](x: ReqAck[T]) {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // source functions

    /**
     * Initialize a source channel. A clock object is associated with the channel
     * which is used to advance simulation time.
     * @param tick clock object marking simulation ticks
     * @return this
     */
    def initSource(tick: Clock): this.type = {
      ClockResolutionUtils.setClock(HandshakeDriver.handshakeSourceKey, x, tick)
      x.req.poke(false.B)
      this
    }

    /**
     * Retrieve clock object associated with this channel
     * @return simulation tick clock object
     */
    protected def getSourceClock: Clock = {
      ClockResolutionUtils.getClock(
        HandshakeDriver.handshakeSourceKey,
        x,
        x.ack.getSourceClock()
      )
    }

    /**
     * Blocks until the given token has been sent via
     * this handshake channel.
     * @param token the token to be sent
     */
    def send(token: T): Unit = {
      x.data.poke(token) // setup data
      x.req.poke((!x.req.peekBoolean()).B) // toggle request
      val old = x.ack.peekBoolean() // remember current ack state
      while (old == x.ack.peekBoolean()) { // wait for ack to toggle
        getSourceClock.step(1)
      }
      getSourceClock.step(1)
    }

    /**
     * Blocks until the given sequence of tokens has been sent through
     * this handshake channel.
     *
     * @param tokens sequence of tokens
     */
    def send(tokens: Seq[T]): Unit = {
      for (elt <- tokens) {
        send(elt)
      }
    }

    /**
     * Blocks until the given sequence of tokens has been sent through
     * this handshake channel.
     * @param token first token
     * @param tokens other tokens
     */
    def send(token: T, tokens: T*): Unit = send(token +: tokens)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // sink functions

    /**
     * Initialize a sink channel. A clock object is associated with the channel
     * which is used to advance simulation time.
     * @param tick clock object marking simulation ticks
     * @return this
     */
    def initSink(tick: Clock): this.type = {
      ClockResolutionUtils.setClock(HandshakeDriver.handshakeSinkKey, x, tick)
      x.ack.poke(false.B)
      this
    }

    /**
     * Retrieve clock object associated with this channel
     * @return simulation tick clock object
     */
    protected def getSinkClock: Clock = {
      ClockResolutionUtils.getClock(
        HandshakeDriver.handshakeSinkKey,
        x,
        x.req.getSourceClock()
      )
    }

    /**
     * Blocks until a new token is available on this channel.
     */
    def waitForToken(): Unit = {
      val old = x.req.peekBoolean()
      getSinkClock.step(1)
      while (old == x.req.peekBoolean()) {
        getSinkClock.step(1)
      }
    }

    /**
     * Blocks until a new token is available. The handshake is completed
     * and the token is returned.
     * @return the received token
     */
    def receive(): T = {
      waitForToken()
      val payload = x.data.peek()
      getSinkClock.step(1)
      x.ack.poke((!x.ack.peekBoolean()).B)
      getSinkClock.step(1)
      payload
    }

    /**
     * Completes n handshakes on this channel and returns all tokens.
     * @param n number of handshakes to complete
     */
    def receive(n: Int): Seq[T] = {
      Seq.fill(n)(receive())
    }

    /**
     * Completes one handshake and checks that the token equals the expected one.
     * @param token the expected token
     */
    def receiveExpect(token: T): Unit = {
      waitForToken()
      x.data.expect(token)
      getSinkClock.step(1)
      x.ack.poke((!x.ack.peekBoolean()).B)
    }

    /**
     * Completes a handshake for each given token and checks that the received
     * token equals the expected one.
     * @param tokens the expected tokens
     */
    def receiveExpect(tokens: Seq[T]): Unit = {
      for(token <- tokens) {
        receiveExpect(token)
      }
    }

    /**
     * Completes a handshake for each given token and checks that the received
     * token equals the expected one.
     * @param token the first token
     * @param tokens other tokens
     */
    def receiveExpect(token: T, tokens: T*): Unit = receiveExpect(token +: tokens)

  }

  /**
   * Contains the keys for the clock object look-up to retreive the clock associated with
   * this handshake port
   */
  object HandshakeDriver {
    protected val handshakeSourceKey = new Object()
    protected val handshakeSinkKey = new Object()
  }

}
