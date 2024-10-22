const DeleteButton = ({ onClick }: { onClick: () => void }) => {
    return (
        <button className="delete-btn mt-[12px]" onClick={onClick}>
            <img
                src="https://www.svgrepo.com/show/21045/delete-button.svg"
                alt="btn image"
                className="h-5 w-5 cursor-pointer"
            />
        </button>
    );
};

export default DeleteButton;
