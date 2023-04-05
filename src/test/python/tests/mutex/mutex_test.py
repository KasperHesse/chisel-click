import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def bimutex_requests(dut):
    """It should ensure mutual exclusion of two producers"""
    #To test: Trigger R1, step a bit, check if g1 is high.
    #Trigger R2, step a bit, check if g2 is high
    # Reset
    dut.io_R1.value = 0
    dut.io_R2.value = 0
    await Timer(3, "ns")

    #Toggle R1, should toggle G1
    dut.io_R1.value = 1
    dut.io_R2.value = 0
    await Timer(1, "ns")
    assert dut.io_G1.value == 1
    assert dut.io_G2.value == 0
    await Timer(1, "ns")

    #Toggle R2, should not toggle G2 but keep G1 high
    dut.io_R2.value = 1
    await Timer(1, "ns")
    assert dut.io_G1.value == 1
    assert dut.io_G2.value == 0
    await Timer(1, "ns")

    #Toggle R1, should take G1 low and G2 high
    dut.io_R1.value = 0
    await Timer(1, "ns")
    assert dut.io_G1.value == 0
    assert dut.io_G2.value == 1
    await Timer(1, "ns")

    #Toggle R2, should take G2 low
    dut.io_R2.value = 0
    await Timer(1, "ns")
    assert dut.io_G1.value == 0
    assert dut.io_G2.value == 0
    await Timer(1, "ns")

    #Toggle R2 again to ensure it can take G2 high
    dut.io_R2.value = 1
    await Timer(1, "ns")
    assert dut.io_G1.value == 0
    assert dut.io_G2.value == 1

