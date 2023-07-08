import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import RisingEdge, FallingEdge


@cocotb.test()
async def my_first_test(dut):
    """We attempt to drive stuff onto the DUT"""
    # Reset and setup
    dut.reset.value = 1
    dut.io_in_req.value = 0
    dut.io_in_data.value = 8
    dut.io_out_ack.value = 0
    # Take reset down

    await Timer(1, units="ns")
    dut.reset.value = 0
    await Timer(1, units="ns")

    # Poke in.req high, wait for out.req
    dut.io_in_req.value = 1
    await RisingEdge(dut.io_out_req)
    await Timer(2, units="ns")
    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == 8
    dut.io_out_ack.value = 1  # out.ack propagates immediately to in.ack
    dut.io_in_req.value = 0
    await FallingEdge(dut.io_out_req)


@cocotb.test()
async def req_low_no_ack(dut):
    """Taking in.req low while in.ack has not become 1 should cause out.req to stay high"""
    # Reset and setup
    dut.reset.value = 1
    dut.io_in_req.value = 0
    dut.io_in_data.value = 8
    dut.io_out_ack.value = 0
    # Take reset down

    await Timer(1, units="ns")
    dut.reset.value = 0
    await Timer(1, units="ns")

    # Poke in.req high, wait for out.req
    dut.io_in_req.value = 1
    await RisingEdge(dut.io_out_req)
    dut.io_in_req.value = 0
    await Timer(15, "ns")
    assert dut.io_out_req.value == 1
