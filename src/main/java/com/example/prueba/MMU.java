package com.example.prueba;

import java.util.*;

public class MMU {

    private int remainingRAM; // Remaining space in the physical memory
    private final List<Page> virtualMemory; // Virtual memory
    private final Page[] realMemory; // Real memory  (physical memory/RAM)
    private final Map<Integer, List<Page>> symbolTable; // Symbol table (Memory map)
    private final Stack<Page> mruPageStack; // Stack to store the most recently used pages
    private List<String> instructions; // List of instructions to executeAll
    private int pageIdCounter; // Counter for the page ID
    private int currentIndex = 0; // Index for MRU algorithm
    private int ptrCounter;  // Pointer counter for id
    private int paginationAlgorithm; // Number of the pagination algorithm chosen by the user
    private int pageFaults; // Number of page faults
    private int pageHits; // Time to access a page in memory in seconds
    private final Set<Integer> processesIds; // Set to store the ids of the processes in execution
    private final Map<Page, Integer> fragmentedPages; // Map to store the pages with fragmentation

    public MMU() {
        this.remainingRAM = Computer.MAX_RAM_PAGES;
        this.virtualMemory = new ArrayList<>();
        this.realMemory = new Page[Computer.MAX_RAM_PAGES];
        this.symbolTable = new HashMap<>();
        this.mruPageStack = new Stack<>();
        this.processesIds = new HashSet<>();
        this.fragmentedPages = new HashMap<>();
        this.ptrCounter = 1;
        this.paginationAlgorithm = 0;
        this.pageFaults = 0;
        this.instructions = null;
        this.pageIdCounter = 1;
    }

    public Integer getFragmentation() {
        return fragmentedPages.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getPageFaults() {
        return pageFaults;
    }

    public int getPageHits() {
        return pageHits;
    }

    public int getPageHitTime () {
        return pageHits * Computer.PAGE_HIT_TIME;
    }

    public int getTrashingTime() {
        return pageFaults * Computer.DISK_ACCESS_TIME_SECONDS;
    }

    public int getTotalTime () {
        return getPageHitTime() + getTrashingTime();
    }

    public int getAmountOfProcesses() {
        return processesIds.size();
    }

    public int getUsedRam() {
        return Computer.PAGE_SIZE_KB * (Computer.MAX_RAM_PAGES - remainingRAM) ;
    }

    public int getUsedRamPercentage() {
        return (getUsedRam() * 100) / (Computer.PAGE_SIZE_KB * Computer.MAX_RAM_PAGES);
    }

    public int getUsedVM() {
        return Computer.PAGE_SIZE_KB * virtualMemory.size();
    }

    public int getUsedVMPercentage() {
        return (getUsedVM() * 100) / (Computer.PAGE_SIZE_KB * Computer.MAX_RAM_PAGES);
    }

    public int getPercentageOfTrashingTime() {
        return (getTrashingTime() * 100) / getTotalTime();
    }

    public List<Page> getRealMemory() {
        return Arrays.asList(realMemory);
    }

    public void setOptimalAlgorithm(List<String> instructions) {
        paginationAlgorithm = 5;
        this.instructions = instructions;
    }

    public void setPaginationAlgorithm(int paginationAlgorithm) {
        this.paginationAlgorithm = paginationAlgorithm;
    }

    /*
     * Create a new process in the memory
     * @param pid The process ID
     * @param size The size of the process in bytes
     * @return The pointer in the real memory where the process is stored
     */
    private Integer new_(Integer pid, Integer size) {

        processesIds.add(pid);

        // Calculate the number of pages needed to store the process
        int remainingPages = calculatePagesNeeded(size);

        // Calculate the fragmentation of the final page
        int fragmentation = calculateFragmentation(size);

        // Check if there is enough space in the real memory to store the process
        if (remainingRAM >= remainingPages) {
            storeNewPages(pid, remainingPages, fragmentation);
            pageHits += remainingPages; // Increase the page hits
        } else {
            // If there is not enough space, then free the memory using the chosen pagination algorithm
            paginationAlgorithm(remainingPages);
            storeNewPages(pid, remainingPages, fragmentation);

            pageHits += remainingRAM; // Increase the page hits
            pageFaults += remainingPages - remainingRAM; // Increase the page faults
        }

        return ptrCounter - 1;
    }

    /*
     * Use the memory assigned to the given pointer
     * @param ptr The pointer to the memory assigned to the process
     * @throws NotInRealMemoryException If the pointer is not in the real memory
     */
    private void use(int ptr) throws Exception {
        if (!symbolTable.containsKey(ptr)) {
            throw new Exception("The pointer " + ptr + " is not in the symbol table");
        }

        List<Page> ptrPages = symbolTable.get(ptr); // Get the pages assigned to the pointer
        List<Page> pagesToMove = new ArrayList<>(); // List to store the pages that need to be moved to the real memory
        Set<Page> realMemorySet = new HashSet<>(Arrays.asList(realMemory)); // Set to store the pages in the real memory

        // Iterate over the pages assigned to the pointer
        for (Page searchedPage : ptrPages) {
            // Check if the page is in the real memory
            if (realMemorySet.contains(searchedPage)) {
                if (paginationAlgorithm == 2) { // If the pagination algorithm is SC
                    searchedPage.setReferenceBit(true);
                }
                pageHits++; // Increase the page hits
                searchedPage.setLoadedTime(Computer.PAGE_HIT_TIME);
            } else {
                pageFaults++; // Increase the page faults
                pagesToMove.add(searchedPage); // Add the page to the list of pages to move to the real memory
                searchedPage.setLoadedTime(Computer.DISK_ACCESS_TIME_SECONDS);
            }
        }

        // Check if there are pages to move to the real memory
        if (!pagesToMove.isEmpty()) {
            paginationAlgorithm(pagesToMove.size()); // Free the memory using the chosen pagination algorithm
            storeOldPages(pagesToMove, ptr); // Store the pages in the real memory
            virtualMemory.removeAll(pagesToMove); // Remove the pages from the virtual memory
        }
        // If the pagination algorithm is MRU
        if (paginationAlgorithm == 3)
            mruPageStack.addAll(ptrPages); // Add the pages to the stack of most recently used pages

    }

    /*
     * Delete all the pages in the real memory that belong to the given pointer
     * @param ptr The pointer to the memory assigned to the process
     */
    private void delete(int ptr) throws Exception {
        // Check if the pointer is in the symbol table
        if (!symbolTable.containsKey(ptr)) {
            throw new Exception("The pointer " + ptr + " is not in the symbol table");
        }

        // Get the pages assigned to the pointer
        List<Page> pagesToRemove = symbolTable.get(ptr);

        // Iterate over the real memory to free the memory used by the pointer
        for (Page page : pagesToRemove) {
            if (page.getInRealMemory()) {
                for (int i = 0; i < realMemory.length; i++) {
                    if (realMemory[i] != null && realMemory[i] == page) {
                        if (paginationAlgorithm == 3)
                            mruPageStack.remove(page); // Remove the page from the stack of most recently used pages
                        fragmentedPages.remove(page); // Remove the page from the fragmented pages if it exists
                        realMemory[i] = null;
                        remainingRAM++;
                    }
                }
            } else {
                if (paginationAlgorithm == 3)
                    mruPageStack.remove(page); // Remove the page from the stack of most recently used pages
                virtualMemory.remove(page);
            }
        }

        // Remove the pointer from the symbol table
        symbolTable.remove(ptr);

    }

    /*
     * Kill the process with the given PID
     * @param pid The process ID
     */
    private void kill(Integer pid) throws Exception{

        processesIds.remove(pid);

        List<Integer> pointersToRemove = new ArrayList<>();
        // Iterate over the real memory to free the memory used by the process
        for (int i = 0; i < realMemory.length; i++) {
            if (realMemory[i] != null && Objects.equals(realMemory[i].getPId(), pid)) {
                // Add the pointer to the list of pointers to remove from the symbol table
                if (!pointersToRemove.contains(realMemory[i].getPhysicalAddress())) {
                    pointersToRemove.add(realMemory[i].getPhysicalAddress());
                }
                if (paginationAlgorithm == 3)
                    mruPageStack.remove(realMemory[i]); // Remove the page from the stack of most recently used pages
                realMemory[i] = null;
                remainingRAM++;
            }
        }

        // Remove the pointers with their pages from the symbol table
        for (Integer ptr : pointersToRemove) {
            symbolTable.remove(ptr);
        }

        // Remove the fragmented pages if they belong to the process
        Map<Page, Integer> fragmentedPagesCopy = new HashMap<>(fragmentedPages);
        fragmentedPagesCopy.forEach((page, fragmentation) -> {
            if (page.getPId() == pid)
                fragmentedPages.remove(page);
        });

        // Remove the pages from the virtual memory
        boolean removedVM = virtualMemory.removeIf(page -> Objects.equals(page.getPId(), pid));

        // Check if the process is not in the memory
        if (pointersToRemove.isEmpty() && !removedVM) {
            throw new Exception("The process with PID " + pid + " is not in the memory");
        }
    }

    /*
        -------------------------------------
        PAGINATION ALGORITHMS
        -------------------------------------
    */

    /*
     * FIFO algorithm to free memory (First In First Out)
     * @param remainingPages The number of pages needed to store the process
     */
    private void fifo(int remainingPages) {

        int iterator = 0;
        // Iterate over the real memory to free the memory needed to store the new pages
        while (remainingRAM < remainingPages) {
            // If the space is occupied, then move the page to the virtual memory to free the space
            if (realMemory[iterator] != null) {
                Page pageToMove = realMemory[iterator];
                pageToMove.setPhysicalAddress(null);
                pageToMove.setInRealMemory(false);
                pageToMove.setIndexOnMemory(-1);
                virtualMemory.add(pageToMove);
                realMemory[iterator] = null; // Free the space in the real memory
                remainingRAM++; // Increase the remaining RAM
            }

            // Move the iterator to the next position. If it reaches the end, then start from the beginning
            iterator++;
            if (iterator == Computer.MAX_RAM_PAGES) {
                iterator = 0;
            }
        }
    }

    /*
     * SC algorithm to free memory (Second Chance)
     * @param remainingPages The number of pages needed to store the process
     */
    private void sc(int remainingPages) {
        int iterator = 0;

        // Iterate over the real memory to free the memory needed to store the new pages
        while (remainingRAM < remainingPages) {
            // Get the page in the current position
            Page page = realMemory[iterator];
            // If the space is occupied, then check the reference bit
            if (page != null) {
                if (page.getReferenceBit()) {
                    // If the reference bit is true, then set it to false
                    page.setReferenceBit(false);
                } else {
                    // If the reference bit is false, then move the page to the virtual memory to free the space
                    page.setPhysicalAddress(null);
                    page.setInRealMemory(false);
                    page.setIndexOnMemory(-1);
                    virtualMemory.add(page);
                    realMemory[iterator] = null;
                    remainingRAM++;
                }
            }
            // Move the iterator to the next position. If it reaches the end, then start from the beginning
            iterator++;
            if (iterator == Computer.MAX_RAM_PAGES) {
                iterator = 0;
            }
        }
    }

    /*
     * MRU algorithm to free memory (Most Recently Used)
     * @param remainingPages The number of pages needed to store the process
     */
    private void mru(int remainingPages) {

        // Iterate over the real memory to find the most recently used page
        while (remainingRAM < remainingPages) {
            try {
                Page mruPage = mruPageStack.pop();
                for (int i = 0; i < realMemory.length; i++) {
                    // If the page is the most recently used, then move it to the virtual memory
                    if (realMemory[i] != null && realMemory[i] == mruPage) {
                        mruPage.setPhysicalAddress(null);
                        mruPage.setInRealMemory(false);
                        mruPage.setIndexOnMemory(-1);
                        virtualMemory.add(mruPage);
                        realMemory[i] = null;
                        remainingRAM++;  // Increase the remaining RAM
                        break;
                    }
                }
            }
            catch (EmptyStackException e){
                rnd(remainingPages);
            }
        }
    }

    /*
     * Random algorithm to free memory (Random)
     * @param remainingPages The number of pages needed to store the process
     */
    private void rnd(int remainingPages) {
        Random random = new Random();

        // Iterate over the real memory to free the memory needed to store the new pages
        while (remainingRAM < remainingPages) {
            // Generate a random index to choose a page to move to the virtual memory
            int randomIndex = random.nextInt(Computer.MAX_RAM_PAGES);
            // Get the page in the random index
            Page page = realMemory[randomIndex];
            // If the space is occupied, then move the page to the virtual memory to free the space
            if (page != null) {
                page.setPhysicalAddress(null);
                page.setInRealMemory(false);
                page.setIndexOnMemory(-1);
                virtualMemory.add(page);
                realMemory[randomIndex] = null;
                remainingRAM++;
            }
        }
    }

    /*
     * Optimal algorithm to free memory
     * @param remainingPages The number of pages needed to store the process
     */
    private void optimal(int remainingPages) {
        while (remainingRAM < remainingPages) {
            int farthestAccess = -1;
            int pageToReplaceIndex = -1;

            // Iterate over the pages in the real memory
            for (int i = 0; i < realMemory.length; i++) {
                Page page = realMemory[i];
                if (page == null) continue;
                int nextPageAccess = findNextPageAccess(page, instructions);

                // If the page will not be accessed in the future or its next access is farther, update
                if (nextPageAccess == -1){
                    pageToReplaceIndex = i;
                    break;
                }

                // If the page will be accessed in the future and its next access is farther, update
                if (nextPageAccess > farthestAccess){
                    farthestAccess = nextPageAccess;
                    pageToReplaceIndex = i;
                }
            }

            // If no pages are found in real memory, break
            if (pageToReplaceIndex == -1) break;
            // Remove the page from real memory and increase remaining RAM
            Page pageToReplace = realMemory[pageToReplaceIndex];
            pageToReplace.setPhysicalAddress(null);
            pageToReplace.setIndexOnMemory(-1);
            pageToReplace.setInRealMemory(false);
            virtualMemory.add(pageToReplace);
            realMemory[pageToReplaceIndex] = null;
            remainingRAM++;
        }
    }

    /*
        -------------------------------------
        AUXILIARY METHODS
        -------------------------------------
    */

    /*
     * Store the new pages in the real memory
     * @param pid The process ID
     * @param remainingPages The number of pages needed to store the process
     */
    private void storeNewPages(Integer pid, int remainingPages, int fragmentation) {

        // Create a list to store the references to the new pages on the symbol table
        List<Page> pages = new ArrayList<>();

        // Iterate over the real memory to store the new pages in the empty spaces
        int ramIterator = 0;
        while (remainingPages > 0) {
            if (realMemory[ramIterator] == null) {
                // Create a new page and store it in the real memory
                Page page = new Page(pid, pageIdCounter++);
                if (remainingPages == 1) {
                    fragmentedPages.put(page, fragmentation);
                }
                page.setInRealMemory(true);
                page.setPhysicalAddress(ptrCounter);
                page.setIndexOnMemory(ramIterator);

                realMemory[ramIterator] = page;
                pages.add(page); // Add the page to the list of pages to store in the symbol table
                remainingRAM--; // Decrease the remaining RAM
                remainingPages--;
                if (paginationAlgorithm == 3) { // If the pagination algorithm is MRU
                    mruPageStack.push(page); // Add the page to the stack of most recently used pages
                }
            }

            // Move the iterator to the next position. If it reaches the end, then start from the beginning
            if (ramIterator == Computer.MAX_RAM_PAGES - 1) {
                ramIterator = 0;
            } else {
                ramIterator++;
            }
        }

        // Store the references to the pages in the symbol table
        symbolTable.put(ptrCounter, pages);
        ptrCounter++;
    }

    /*
     * Store the pages in the real memory
     * @param pages The pages that already exist in the real memory
     * @param ptr The pointer to the memory assigned to the process
     */
    private void storeOldPages(List<Page> pages, int ptr) {
        int ramIterator = 0;

        // Iterate over the real memory to store the pages in the empty spaces
        while (remainingRAM > 0) {
            for (Page page : pages) {
                if (realMemory[ramIterator] == null) {
                    // Store the page in the real memory
                    page.setInRealMemory(true);
                    page.setPhysicalAddress(ptr);
                    page.setIndexOnMemory(ramIterator);
                    realMemory[ramIterator] = page;
                    remainingRAM--; // Decrease the remaining RAM
                }

                // Move the iterator to the next position. If it reaches the end, then start from the beginning
                if (ramIterator == Computer.MAX_RAM_PAGES - 1) {
                    ramIterator = 0;
                } else {
                    ramIterator++;
                }
            }
        }
    }

    /*
     * Choose a pagination algorithm to free memory. If the algorithm has already been chosen, then executeAll it.
     * @param remainingPages The number of pages needed to store the process
     */
    public void paginationAlgorithm(int remainingPages) {
        // Execute the chosen pagination algorithm
        switch (paginationAlgorithm) {
            case 1:
                fifo(remainingPages);
                break;
            case 2:
                sc(remainingPages);
                break;
            case 3:
                mru(remainingPages);
                break;
            case 4:
                rnd(remainingPages);
                break;
            case 5:
                optimal(remainingPages);
                break;
        }
    }

    /*
     * Choose a pagination algorithm to execute
     */
    public void choosePaginationAlgorithm() {
        // If the pagination algorithm has not been chosen, then ask the user to choose one
        if (paginationAlgorithm == 0) {
            Scanner scanner = new Scanner(System.in);

            // Ask the user to choose a pagination algorithm until a valid option is chosen
            do {
                try {
                    System.out.println("Choose a pagination algorithm ( 1 - 4): ");
                    System.out.println("1. FIFO");
                    System.out.println("2. SC");
                    System.out.println("3. MRU");
                    System.out.println("4. RND");
                    paginationAlgorithm = scanner.nextInt();
                    if (paginationAlgorithm < 1 || paginationAlgorithm > 4) {
                        System.err.println("Invalid option, please choose a number between 1 and 4");
                    }
                } catch (InputMismatchException e) {
                    System.err.println("Invalid option, please choose a number between 1 and 4");
                    scanner.next();
                }
            } while (paginationAlgorithm < 1 || paginationAlgorithm > 4);
        }
    }

    /*
     * Find the next access to a page in the instructions list
     * @param page The page to find next access for
     * @param instructions The list of instructions
     * @return The index of next access or -1 if not found
     */
    private int findNextPageAccess(Page page, List<String> instructions) {
        for (int i = currentIndex; i < instructions.size(); i++) {
            String instruction = instructions.get(i);
            if (instruction.contains("use(" + page.getPhysicalAddress() + ")")) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Calculate the number of pages needed to store the process
     * @param size The size of the process in bytes
     * @return The number of pages needed to store the process
     */
    private int calculateFragmentation(int size){
        // If the size is greater than 1KB, then calculate the fragmentation
        if (size % Computer.KB != 0) {
            return Computer.KB - size % Computer.KB;
            // If the size is less than 1KB, the fragmentation is the difference between 1KB and the size
        } else if (size < Computer.KB) {
            return Computer.KB - size;
        } else {
            return 0;
        }
    }

    /*
     * Calculate the number of pages needed to store the pointer
     * @param size The size of the pointer in bytes
     * @return The number of pages needed to store the pointer
     */
    private int calculatePagesNeeded(Integer size) {
        int result;
        // Check if the size is greater than 1KB
        if (size > Computer.KB) {
            // Calculate the number of pages needed
            result = size / Computer.KB;
            int residue = size % Computer.KB;
            if (residue > 0) {
                result++;
            }
        } else {
            // If the size is less than 1KB, then only one page is needed
            result = 1;
        }
        return result;
    }

    /*
     * Execute the given instruction
     * @param instruction The instruction to executeAll
     */
    public void executeInstruction(String instruction) {
        // Ask the user to choose a pagination algorithm if it has not been chosen

        if (paginationAlgorithm == 0) {
            choosePaginationAlgorithm();
        }

        // Initialize the variables to store the command, process ID, size, command, pointer
        Integer pid = null;
        Integer size = null;
        String command;
        Integer ptr = null;

        // Split the instruction to get the command and the arguments
        String[] parts = instruction.split("\\(");
        command = parts[0];
        if (command.equals("new")) {
            String[] args = parts[1].split(",");
            pid = Integer.parseInt(args[0]);
            size = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
        } else if (command.equals("kill")) {
            pid = Integer.parseInt(parts[1].substring(0, parts[1].length() - 1));
        } else {
            ptr = Integer.parseInt(parts[1].substring(0, parts[1].length() - 1));
        }
        // Execute the command depending on the instruction
        try {
            switch (command) {
                case "new":
                    new_(pid, size);
                    break;
                case "delete":
                    delete(ptr);
                    break;
                case "kill":
                    kill(pid);
                    break;
                case "use":
                    use(ptr);
                    break;
                default:
                    System.out.println("Invalid command: " + command);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        currentIndex++;
    }

    /*
     * Execute the instructions in the given list
     * @param instructions The list of instructions to executeAll
     */
    public void executeAll(List<String> instructions) {

        // Ask the user to choose a pagination algorithm if it has not been chosen
        if (paginationAlgorithm == 0) {
            choosePaginationAlgorithm();
        }

        // Check if there are instructions to executeAll
        if (instructions == null) {
            System.out.println("No instructions to executeAll");
            return;
        }

        // Ask the user to choose a pagination algorithm if it has not been chosen
        if (paginationAlgorithm == 0) {
            choosePaginationAlgorithm();
        }

        // Iterate over the instructions to executeAll them
        for (String instruction : instructions) {
            executeInstruction(instruction);
        }
    }


    /*
     * Print the pages in the real memory
     */
    public void printRealMemory() {
        System.out.println("=================================");
        System.out.println("Real memory: ");
        int iter = 0;
        for (Page page : realMemory) {
            if (page != null) {
                System.out.print(iter++ + " ");
                System.out.println(page);
            } else {
                System.out.println(iter++ + " null");
            }
        }
        System.out.println("=================================");
    }

    /*
     * Print the pages in the virtual memory
     */
    public void printVirtualMemory() {
        System.out.println("=================================");
        System.out.println("\nVirtual memory: ");
        for (Page page : virtualMemory) {
            System.out.println(page);
        }
        System.out.println("=================================");
    }

    /*
     * Print the symbol table
     */
    public void printSymbolTable() {
        System.out.println("=================================");
        System.out.println("\nSymbol table: ");
        for (Map.Entry<Integer, List<Page>> entry : symbolTable.entrySet()) {
            System.out.print("Ptr: " + entry.getKey() + ", Pages: ");
            for (Page page : entry.getValue()) {
                System.out.print(page + ", ");
            }
            System.out.println();
        }
        System.out.println("=================================");
    }

    /*
     * Reset the memory management unit
     */
    public void reset() {
        this.remainingRAM = Computer.MAX_RAM_PAGES;
        this.virtualMemory.clear();
        Arrays.fill(realMemory, null);
        this.symbolTable.clear();
        this.mruPageStack.clear();
        this.processesIds.clear();
        this.fragmentedPages.clear();
        this.ptrCounter = 1;
        this.pageFaults = 0;
        this.pageHits = 0;
        this.currentIndex = 0;
        this.pageIdCounter = 1;
        this.remainingRAM = Computer.MAX_RAM_PAGES;
        this.instructions = null;
    }

    /*
     * Get the pages details to display in a JavaFX TableView
     * @return The list of PageDetails objects
     */
    public List<PageDetails> getPagesDetails() {
        List<PageDetails> detailsList = new ArrayList<>();

        // Procesar la memoria real
        for (Page page : realMemory) {
            if (page != null) { // Asegúrate de que la página no esté vacía
                PageDetails details = createPageDetailsFromPage(page, true);
                detailsList.add(details);
            }
        }

        // Procesar una copia de la memoria virtual para evitar ConcurrentModificationException
        List<Page> virtualMemoryCopy = new ArrayList<>(virtualMemory);
        for (Page page : virtualMemoryCopy) {
            PageDetails details = createPageDetailsFromPage(page, false);
            detailsList.add(details);
        }

        return detailsList;
    }

    /*
        * Create a PageDetails object from a Page for JavaFX TableView
        * @param page The page to create the PageDetails object
        * @param inRealMemory A boolean to indicate if the page is in the real memory
     */
    private PageDetails createPageDetailsFromPage(Page page, boolean inRealMemory) {

        String loaded = inRealMemory ? "Yes" : "No";
        String pageId = String.valueOf(page.getId());
        String pid = String.valueOf(page.getPId());
        String lAddr = inRealMemory ? String.valueOf(page.getPhysicalAddress()) : "N/A";
        String mAddr = page.getIndexOnMemory() == -1 ? "" : String.valueOf(page.getIndexOnMemory());
        String loadedT = String.valueOf(page.getLoadedTime());
        String mark = page.getReferenceBit() ? "1" : "0";

        return new PageDetails(pageId, pid, loaded, lAddr, mAddr, loadedT, mark);
    }
}