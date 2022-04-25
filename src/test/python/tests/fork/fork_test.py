import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import RisingEdge, FallingEdge


@cocotb.test()
async def fork_data(dut):
    """It should fork data on both channels, and only take down in.ack once both outputs acknowledge it"""

    # Reset
    dut.reset.value = 1
    dut.io_in_req.value = 0
    dut.io_in_data.value = 0
    dut.io_out1_ack.value = 0
    dut.io_out2_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")

    dut.io_in_data.value = 15
    dut.io_in_req.value = 1
    await Timer(1, "ns")

    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 1
    assert dut.io_out1_data.value == 15
    assert dut.io_out2_data.value == 15

    assert dut.io_in_ack.value == 0

    dut.io_out1_ack.value = 1
    await Timer(1, "ns")
    assert dut.io_in_ack.value == 0

    dut.io_out1_ack.value = 0
    dut.io_out2_ack.value = 1
    await Timer(1, "ns")
    assert dut.io_in_ack.value == 0

    dut.io_out1_ack.value = 1
    await Timer(1, "ns")
    assert dut.io_in_ack.value == 1
