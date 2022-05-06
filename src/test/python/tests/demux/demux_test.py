import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def demux_data(dut):
    """It should demultiplex data on two output channels"""
    # Reset
    dut.reset.value = 1
    dut.io_in_req.value = 0
    dut.io_sel_req.value = 0
    dut.io_in_data.value = 42
    dut.io_sel_data.value = 0
    dut.io_out1_ack.value = 0
    dut.io_out2_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")

    # Attempt to forward data to the first channel
    # Must bring both in.req and sel.req high to signal new data
    dut.io_in_req.value = 1
    dut.io_sel_req.value = 1
    await Edge(dut.io_out1_req)
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 0
    await Timer(1, "ns")
    dut.io_out1_ack.value = 1
    await Timer(1, "ns")

    # Attempt to forward data to the second channel
    # Must toggle both in.req and sel.req
    dut.io_in_req.value = 0
    dut.io_sel_req.value = 0
    dut.io_sel_data.value = 1
    await Edge(dut.io_out2_req)
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 1
    dut.io_out2_ack.value = 1
    await Timer(2, "ns")

    # Drive on the first channel
    # It shouldn't forward the output-request until both in.req and sel.req have triggered
    dut.io_in_req.value = 1
    dut.io_sel_data.value = 0
    await Timer(5, "ns")
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 1
    dut.io_sel_req.value = 1
    await Edge(dut.io_out1_req)
    assert dut.io_out1_req.value == 0
    assert dut.io_out2_req.value == 1
    dut.io_out1_ack.value = 0
    await Timer(2, "ns")

    # Drive some more on the second channel
    dut.io_sel_req.value = 0
    dut.io_sel_data.value = 1
    await Timer(5, "ns")
    assert dut.io_out1_req.value == 0
    assert dut.io_out2_req.value == 1
    dut.io_in_req.value = 0
    await Edge(dut.io_out2_req)
    assert dut.io_out1_req.value == 0
    assert dut.io_out2_req.value == 0
    await Timer(3, "ns")


