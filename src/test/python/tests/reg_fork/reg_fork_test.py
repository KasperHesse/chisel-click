import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def reg_fork_data(dut):
    """It should fork data on both channels, and only take down in.ack once both outputs acknowledge it"""

    # Reset
    dut.reset.value = 1
    dut.io_in_req.value = 0
    dut.io_in_data.value = 42
    dut.io_out1_ack.value = 0
    dut.io_out2_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")
    assert dut.io_out1_data.value == 4
    assert dut.io_out2_data.value == 4
    assert dut.io_out1_req.value == 0
    assert dut.io_out2_req.value == 0

    dut.io_in_req.value = 1
    await Edge(dut.io_out1_req)
    await Timer(1)
    assert dut.io_out1_data.value == 42
    assert dut.io_out2_data.value == 42
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 1

    await Timer(1, "ns")
    dut.io_in_data.value = 84
    dut.io_in_req.value = 0
    # Should not update data yet
    await Timer(10, "ns")
    assert dut.io_out1_data.value == 42
    assert dut.io_out2_data.value == 42
    await Timer(1)
    dut.io_out1_ack.value = 1
    dut.io_out2_ack.value = 1
    await Edge(dut.io_out1_req)
    await Timer(1)
    assert dut.io_out1_data.value == 84
    assert dut.io_out2_data.value == 84
    assert dut.io_out1_req.value == 0
    assert dut.io_out2_req.value == 0
