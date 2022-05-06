import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def mux_data(dut):
    """It should multiplex between two data channels"""
    # Reset
    dut.reset.value = 1
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    dut.io_sel_req.value = 0
    dut.io_in1_data.value = 42
    dut.io_in2_data.value = 84
    dut.io_sel_data.value = 0
    dut.io_out_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")

    # Attempt to forward the data on first channel
    # Must bring both in1.req and sel.req high to signal new data
    dut.io_in1_req.value = 1
    dut.io_sel_req.value = 1
    await Edge(dut.io_out_req)
    assert dut.io_out_data.value == 42
    await Timer(1, "ns")
    dut.io_out_ack.value = 1
    await Timer(1, "ns")

    # Attempt to forward data on the second channel
    # Must toggle both in2.req and sel.req
    dut.io_in2_req.value = 1
    dut.io_sel_req.value = 0
    dut.io_sel_data.value = 1
    await Edge(dut.io_out_req)
    assert dut.io_out_data.value == 84
    dut.io_out_ack.value = 0
    await Timer(2, "ns")

    # Drive on the first channel
    # It shouldn't forward the output-request until both in.req and sel.req have triggered
    dut.io_in1_req.value = 0
    dut.io_in1_data.value = 13
    dut.io_sel_data.value = 0
    await Timer(5, "ns")
    assert dut.io_out_req.value == 0
    dut.io_sel_req.value = 1
    await Edge(dut.io_out_req)
    assert dut.io_out_data.value == 13
    dut.io_out_ack.value = 1
    await Timer(2, "ns")

    # Drive some more on the second channel
    dut.io_in2_data.value = 10
    dut.io_sel_req.value = 0
    dut.io_sel_data = 1
    await Timer(5, "ns")
    assert dut.io_out_req.value == 1
    dut.io_in2_req.value = 0
    await Edge(dut.io_out_req)
    assert dut.io_out_data == 10


